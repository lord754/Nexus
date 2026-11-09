package com.tungsten.fcl.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mio.util.openLink
import com.tungsten.fcl.databinding.ItemMinecraftNewsBinding
import com.tungsten.fcl.news.MinecraftNewsEntry

/**
 * 主页官方 Minecraft 新闻列表适配器。
 * 点击条目用系统浏览器打开该新闻的 readMoreLink。
 */
class MinecraftNewsAdapter : RecyclerView.Adapter<MinecraftNewsAdapter.Holder>() {

    private val entries = ArrayList<MinecraftNewsEntry>()

    fun submit(list: List<MinecraftNewsEntry>) {
        entries.clear()
        entries.addAll(list)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = entries.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ItemMinecraftNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(entries[position])
    }

    class Holder(private val binding: ItemMinecraftNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: MinecraftNewsEntry) {
            binding.category.text = entry.category
            binding.newsTitle.text = entry.title
            binding.newsDate.text = entry.date
            binding.newsText.text = entry.text
            val link = entry.readMoreLink
            binding.root.setOnClickListener {
                if (link.isNullOrBlank()) return@setOnClickListener
                openLink(binding.root.context, link)
            }
        }
    }
}
