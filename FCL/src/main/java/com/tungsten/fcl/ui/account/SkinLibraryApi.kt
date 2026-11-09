package com.tungsten.fcl.ui.account

import com.google.gson.annotations.SerializedName
import com.tungsten.fclcore.util.io.NetworkUtils
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection

/**
 * MineSkin 公开皮肤列表（GET https://api.mineskin.org/v2/skins）中的一条记录。
 * 列表接口只返回材质哈希，皮肤图片与渲染图按哈希另行取用。
 */
data class SkinLibraryEntry(
    @SerializedName("uuid")
    val uuid: String = "",
    @SerializedName("shortId")
    val shortId: String = "",
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("texture")
    val texture: String = "",
    @SerializedName("timestamp")
    val timestamp: Long = 0L
) {
    /** 列表多数条目没有名称，退回短 id 保证每格都有可读标签 */
    val label: String
        get() = name?.takeIf { it.isNotBlank() } ?: "#$shortId"
}

/** MineSkin 皮肤列表响应；仅关心条目数组 */
data class SkinLibraryResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("skins")
    val skins: List<SkinLibraryEntry>? = null
)

/**
 * 在线皮肤库数据源：条目来自 MineSkin 公开列表，缩略图走 mc-heads.net 渲染，
 * 皮肤原图从 Mojang 材质 CDN 下载（64x64 RGBA，可直接作为本地皮肤）。
 */
interface SkinLibraryApi {

    @GET("v2/skins")
    fun getSkins(@Query("size") size: Int): Call<SkinLibraryResponse>

    companion object {

        private const val BASE_URL = "https://api.mineskin.org/"

        /** 部分接口会拒绝默认的 Java/空 User-Agent，显式声明来源 */
        private const val USER_AGENT = "NexusLauncher/1.0 (Android)"

        /** MineSkin 单次最多返回 128 条，一次拉满可减少请求次数 */
        const val PAGE_SIZE = 96

        private val instance: SkinLibraryApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SkinLibraryApi::class.java)
        }

        /** Retrofit 实例只需构建一次，重复调用复用同一实例 */
        @JvmStatic
        fun create(): SkinLibraryApi = instance

        /** 网格缩略图：mc-heads.net 按材质哈希渲染头部，无需下载整张皮肤 */
        @JvmStatic
        fun thumbnailUrl(entry: SkinLibraryEntry, size: Int): String =
            "https://mc-heads.net/avatar/${entry.texture}/$size"

        /** 皮肤原图地址：Mojang 材质 CDN */
        @JvmStatic
        fun skinUrl(entry: SkinLibraryEntry): String =
            "https://textures.minecraft.net/texture/${entry.texture}"

        /**
         * 下载皮肤 PNG 到 [dest]。
         * 先写同目录临时文件再改名覆盖，避免下载中断把账号正在使用的皮肤文件写坏。
         */
        @JvmStatic
        @Throws(IOException::class)
        fun downloadSkin(entry: SkinLibraryEntry, dest: File): File {
            val temp = File(dest.parentFile, dest.name + ".tmp")
            val connection = NetworkUtils.createHttpConnection(NetworkUtils.toURL(skinUrl(entry)))
            try {
                connection.connectTimeout = 15000
                connection.readTimeout = 20000
                connection.setRequestProperty("User-Agent", USER_AGENT)
                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP ${connection.responseCode}")
                }
                dest.parentFile?.mkdirs()
                connection.inputStream.use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
            } finally {
                connection.disconnect()
            }

            if (!temp.renameTo(dest)) {
                // 少数文件系统不支持覆盖式改名，退回复制
                temp.copyTo(dest, overwrite = true)
                temp.delete()
            }
            return dest
        }
    }
}
