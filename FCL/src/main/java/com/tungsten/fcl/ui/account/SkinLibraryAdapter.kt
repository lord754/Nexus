package com.tungsten.fcl.ui.account

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mio.ui.adapter.ViewHolder
import com.tungsten.fcl.R
import com.tungsten.fcl.databinding.ItemSkinLibraryBinding

/**
 * 在线皮肤库网格适配器：点击整格即选中该皮肤。
 * [onSelect] 只在没有下载中的格子时触发，避免同时下载多张皮肤互相覆盖落位文件。
 */
class SkinLibraryAdapter(
    private val context: Context,
    private val onSelect: (Int, SkinLibraryEntry) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private val list = ArrayList<SkinLibraryEntry>()

    /** 正在下载的格子位置；-1 表示空闲 */
    private var pending = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_skin_library, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = ItemSkinLibraryBinding.bind(holder.itemView)
        val entry = list[position]
        binding.skinName.string = entry.label
        val downloading = position == pending
        // 下载中把缩略图换成进度圈，避免误以为已经应用
        binding.skinProgress.visibility = if (downloading) View.VISIBLE else View.GONE
        binding.skinImage.visibility = if (downloading) View.INVISIBLE else View.VISIBLE
        // 复用视图时必须清掉上一次的请求，否则快速滚动会出现错图
        Glide.with(binding.skinImage).clear(binding.skinImage)
        Glide.with(binding.skinImage)
            .load(SkinLibraryApi.thumbnailUrl(entry, THUMBNAIL_SIZE))
            .error(R.drawable.ic_baseline_account_circle_24)
            .into(binding.skinImage)
        holder.itemView.setOnClickListener { onSelect(position, entry) }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    /** 标记某一格开始下载；已有下载任务时返回 false */
    fun setPending(position: Int): Boolean {
        if (pending != -1) return false
        pending = position
        notifyItemChanged(position)
        return true
    }

    fun clearPending() {
        val previous = pending
        pending = -1
        if (previous != -1) notifyItemChanged(previous)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh(skins: List<SkinLibraryEntry>) {
        list.clear()
        list.addAll(skins)
        pending = -1
        notifyDataSetChanged()
    }

    private companion object {
        /** 缩略图边长（px），网格单元大约 80dp */
        const val THUMBNAIL_SIZE = 200
    }
}
