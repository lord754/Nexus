package com.tungsten.fcl.ui.account

import android.content.Context
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.mio.util.getScreenHeight
import com.mio.util.getScreenWidth
import com.tungsten.fcl.R
import com.tungsten.fcl.databinding.DialogSkinLibraryBinding
import com.tungsten.fclcore.task.Schedulers
import com.tungsten.fclcore.task.Task
import com.tungsten.fclcore.util.Logging
import com.tungsten.fcllibrary.component.dialog.FCLAlertDialog
import com.tungsten.fcllibrary.component.dialog.FCLDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.logging.Level

/**
 * 在线皮肤库：以网格展示社区皮肤，点选后把 PNG 下载到 [destination] 并回调 [onPicked]。
 * 离线账号的皮肤由本地 Yggdrasil 服务在启动时提供，因此落位到 SKIN_DIR 即为生效。
 */
class SkinLibraryDialog(
    context: Context,
    private val destination: File,
    private val onPicked: (String) -> Unit
) : FCLDialog(context), View.OnClickListener {

    private val binding: DialogSkinLibraryBinding =
        DialogSkinLibraryBinding.inflate(layoutInflater)
    private val adapter = SkinLibraryAdapter(context) { position, entry -> pick(position, entry) }

    init {
        setContentView(binding.root)
        setCancelable(false)

        binding.title.string = context.getString(R.string.skin_library)
        binding.recyclerView.layoutManager = GridLayoutManager(context, GRID_SPAN)
        binding.recyclerView.adapter = adapter
        binding.refresh.setOnClickListener(this)
        binding.close.setOnClickListener(this)

        load()
    }

    override fun show() {
        val width = getScreenWidth()
        var height = getScreenHeight()
        height = if (height * 2 < width) {
            WindowManager.LayoutParams.MATCH_PARENT
        } else {
            height * 2 / 3
        }
        window?.setLayout(width * 9 / 10, height)
        super.show()
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.refresh -> load()
            binding.close -> dismiss()
        }
    }

    /** 拉取皮肤列表；刷新按钮与首次进入共用 */
    private fun load() {
        binding.refresh.isEnabled = false
        binding.message.visibility = View.GONE
        binding.recyclerView.visibility = View.INVISIBLE
        binding.progress.visibility = View.VISIBLE
        SkinLibraryApi.create().getSkins(SkinLibraryApi.PAGE_SIZE)
            .enqueue(object : Callback<SkinLibraryResponse> {
                override fun onResponse(
                    call: Call<SkinLibraryResponse>,
                    response: Response<SkinLibraryResponse>
                ) {
                    val skins = response.body()?.skins.orEmpty().filter { it.texture.isNotBlank() }
                    if (!response.isSuccessful || skins.isEmpty()) {
                        showError(context.getString(R.string.skin_library_load_failed))
                        return
                    }
                    adapter.refresh(skins)
                    binding.progress.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.refresh.isEnabled = true
                }

                override fun onFailure(call: Call<SkinLibraryResponse>, t: Throwable) {
                    Logging.LOG.log(Level.WARNING, "Failed to load skin library", t)
                    showError(context.getString(R.string.skin_library_load_failed))
                }
            })
    }

    private fun showError(message: String) {
        binding.progress.visibility = View.GONE
        binding.recyclerView.visibility = View.INVISIBLE
        binding.message.string = message
        binding.message.visibility = View.VISIBLE
        binding.refresh.isEnabled = true
    }

    private fun pick(position: Int, entry: SkinLibraryEntry) {
        if (!adapter.setPending(position)) return
        Task.supplyAsync { SkinLibraryApi.downloadSkin(entry, destination) }
            .whenComplete(Schedulers.androidUIThread()) { file: File?, exception: Exception? ->
                adapter.clearPending()
                if (file == null || exception != null) {
                    Logging.LOG.log(Level.WARNING, "Failed to download skin", exception)
                    val builder = FCLAlertDialog.Builder(context)
                    builder.setAlertLevel(FCLAlertDialog.AlertLevel.ALERT)
                    builder.setMessage(context.getString(R.string.skin_library_apply_failed))
                    builder.setNegativeButton(context.getString(R.string.dialog_positive), null)
                    builder.create().show()
                    return@whenComplete
                }
                onPicked(file.absolutePath)
                Toast.makeText(context, R.string.skin_library_applied, Toast.LENGTH_SHORT).show()
            }.start()
    }

    private companion object {
        const val GRID_SPAN = 3
    }
}
