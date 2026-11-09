package com.mio.discord

import android.content.Context
import android.net.LocalSocket
import android.net.LocalSocketAddress
import android.os.Process
import android.util.Log
import com.google.gson.Gson
import com.tungsten.fcl.R
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.concurrent.Executors

/**
 * Discord Rich Presence 客户端（Discord IPC 传输）。
 *
 * Discord 的 RPC 服务只通过本机 IPC 套接字通信（Linux/macOS 为
 * `${XDG_RUNTIME_DIR}/discord-ipc-{n}`，Windows 为命名管道），
 * 因此需要运行中的 Discord 客户端在同一台设备上；套接字不可达时整体静默降级，
 * 不影响游戏启动。开关方式：在启动器设置中填写 Discord Application ID，留空即禁用。
 */
object DiscordRpc {

    private const val TAG = "DiscordRpc"

    /** launcher SharedPreferences 中存放 Discord Application ID 的键（与应用内其他设置一致） */
    const val PREF_CLIENT_ID = "discord_rpc_client_id"

    private const val PREFS_NAME = "launcher"

    private const val OP_HANDSHAKE = 0
    private const val OP_FRAME = 1
    private const val RPC_VERSION = 1

    private const val SOCKET_PREFIX = "discord-ipc-"

    /** Discord 最多提供 10 个套接字，按序号顺序尝试 */
    private const val MAX_SOCKET_INDEX = 10

    private const val TIMEOUT_MS = 3000

    private val gson = Gson()

    /** 连接状态与收发都在同一线程串行执行，避免并发读写破坏帧边界 */
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "discord-rpc").apply { isDaemon = true }
    }

    private var socket: LocalSocket? = null

    /** 本局游戏开始时间，用于 Rich Presence 的 elapsed 计时 */
    private var startedAt = 0L

    /**
     * 游戏实例启动时广播“Playing Minecraft on Nexus Launcher”。
     * 未配置 Application ID 时直接返回。
     */
    @JvmStatic
    fun setPlaying(context: Context) {
        val clientId = clientId(context)
        if (clientId == null) {
            Log.i(TAG, "Discord Application ID not configured, Rich Presence disabled")
            return
        }
        val details = context.getString(R.string.discord_rpc_details)
        val state = context.getString(R.string.discord_rpc_state)
        startedAt = System.currentTimeMillis()
        executor.execute {
            try {
                ensureConnected(clientId)
                sendActivity(details, state)
                Log.i(TAG, "Discord Rich Presence updated")
            } catch (e: Exception) {
                // Discord 未运行或套接字不可达：关闭连接并放弃本次状态同步
                Log.w(TAG, "Discord Rich Presence unavailable: ${e.message}")
                closeQuietly()
            }
        }
    }

    /** 游戏实例结束时清除仍是本启动器设置的状态 */
    @JvmStatic
    fun clear() {
        executor.execute { closeQuietly() }
    }

    private fun clientId(context: Context): String? = context
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(PREF_CLIENT_ID, null)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    /**
     * Discord 解析 IPC 路径的顺序：XDG_RUNTIME_DIR → TMPDIR → TMP → TEMP → /tmp，
     * 另外补上 snap / flatpak 版 Discord 的运行时子目录。
     */
    private fun socketPaths(): List<String> {
        val directories = LinkedHashSet<String>()
        for (key in arrayOf("XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP")) {
            val value = System.getenv(key)
            if (!value.isNullOrBlank()) {
                directories.add(value.trimEnd('/'))
            }
        }
        directories.add("/tmp")
        val runtime = System.getenv("XDG_RUNTIME_DIR")
        if (!runtime.isNullOrBlank()) {
            val base = runtime.trimEnd('/')
            directories.add("$base/app/com.discordapp.Discord")
            directories.add("$base/snap.discord")
        }
        return directories.flatMap { directory ->
            (0 until MAX_SOCKET_INDEX).map { index -> "$directory/$SOCKET_PREFIX$index" }
        }
    }

    /** 复用已建立的连接，否则按候选路径逐个尝试；全部失败时抛 IOException */
    private fun ensureConnected(clientId: String) {
        if (socket != null) {
            return
        }
        for (path in socketPaths()) {
            val candidate = LocalSocket()
            try {
                candidate.connect(
                    LocalSocketAddress(path, LocalSocketAddress.Namespace.FILESYSTEM)
                )
                candidate.setSoTimeout(TIMEOUT_MS)
                socket = candidate
                handshake(clientId)
                return
            } catch (e: IOException) {
                closeQuietly(candidate)
            }
        }
        throw IOException("Discord IPC socket not found")
    }

    /** 握手：发送 HANDSHAKE 并读取 Discord 的 READY 帧确认 Application ID 被接受 */
    private fun handshake(clientId: String) {
        writeFrame(OP_HANDSHAKE, gson.toJson(mapOf("v" to RPC_VERSION, "client_id" to clientId)))
        val ready = readFrame()
        if (ready.contains("\"evt\":\"ERROR\"")) {
            throw IOException("Discord rejected the application id: $ready")
        }
    }

    private fun sendActivity(details: String, state: String) {
        val activity = linkedMapOf<String, Any>(
            "details" to details,
            "state" to state,
            "timestamps" to mapOf("start" to startedAt)
        )
        val payload = linkedMapOf<String, Any>(
            "cmd" to "SET_ACTIVITY",
            "args" to mapOf("pid" to Process.myPid(), "activity" to activity),
            "nonce" to UUID.randomUUID().toString()
        )
        writeFrame(OP_FRAME, gson.toJson(payload))
    }

    /** 帧格式：4 字节小端 opcode + 4 字节小端 JSON 长度 + UTF-8 JSON */
    private fun writeFrame(opcode: Int, payload: String) {
        val current = socket ?: throw IOException("Discord IPC socket is not connected")
        val body = payload.toByteArray(StandardCharsets.UTF_8)
        val header = ByteBuffer.allocate(8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(opcode)
            .putInt(body.size)
            .array()
        val output = current.outputStream
        output.write(header)
        output.write(body)
        output.flush()
    }

    private fun readFrame(): String {
        val current = socket ?: throw IOException("Discord IPC socket is not connected")
        val input = current.inputStream
        val header = ByteArray(8)
        readFully(input, header)
        val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)
        buffer.int
        val length = buffer.int
        if (length <= 0) {
            return ""
        }
        val body = ByteArray(length)
        readFully(input, body)
        return String(body, StandardCharsets.UTF_8)
    }

    private fun readFully(input: InputStream, target: ByteArray) {
        var offset = 0
        while (offset < target.size) {
            val read = input.read(target, offset, target.size - offset)
            if (read < 0) {
                throw IOException("Unexpected end of Discord IPC stream")
            }
            offset += read
        }
    }

    private fun closeQuietly() {
        val current = socket ?: return
        socket = null
        closeQuietly(current)
    }

    private fun closeQuietly(target: LocalSocket) {
        try {
            target.close()
        } catch (_: Exception) {
            // 关闭失败无需处理，连接已被丢弃
        }
    }
}
