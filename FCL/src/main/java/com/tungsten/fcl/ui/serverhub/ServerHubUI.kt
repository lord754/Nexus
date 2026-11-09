package com.tungsten.fcl.ui.serverhub

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tungsten.fcl.R
import com.tungsten.fcl.activity.MainActivity
import com.tungsten.fcllibrary.component.ui.FCLCommonUI

/**
 * 服务器中心页：展示内置热门服务器，点击「加入」即以当前选中的版本启动，
 * 并把该服务器的 host:port 作为本次启动的服务器地址（--server/--port）传给游戏。
 */
class ServerHubUI(context: Context, id: Int) : FCLCommonUI(context, id) {

    override fun onCreate() {
        super.onCreate()

        val recyclerView = findViewById<RecyclerView>(R.id.server_list)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = ServerHubAdapter(HotServers.LIST) { server ->
            MainActivity.getInstance().launchVersionWithServer(server.address)
        }
    }
}
