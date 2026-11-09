package com.tungsten.fcl.ui.serverhub

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mio.ui.adapter.ViewHolder
import com.tungsten.fcl.R
import com.tungsten.fcl.databinding.ItemServerHubBinding

/** 服务器中心列表适配器：每张卡片右下角的「加入」按钮触发直连启动 */
class ServerHubAdapter(
    private val servers: List<HotServer>,
    private val onJoin: (HotServer) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_server_hub, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = ItemServerHubBinding.bind(holder.itemView)
        val server = servers[position]
        binding.iconText.string = server.badge
        // 图标底色按服务器区分；解析失败时保持布局里的默认色
        runCatching { Color.parseColor(server.tint) }.onSuccess {
            binding.iconTile.backgroundTintList = ColorStateList.valueOf(it)
        }
        binding.serverName.string = server.name
        binding.serverTag.string = server.tag
        binding.serverAddress.string = server.address
        binding.serverDesc.string = server.description
        binding.join.setOnClickListener { onJoin(server) }
    }

    override fun getItemCount(): Int {
        return servers.size
    }
}
