package com.tungsten.fcl.ui.serverhub

/**
 * 服务器中心里的内置服务器条目。
 * [host] 与 [port] 会以 host:port 形式作为本次启动的服务器地址传给游戏，
 * 图标使用 [tint] 底色的首字母方块（不内置第三方图标文件）。
 */
data class HotServer(
    val name: String,
    /** 图标上的缩写 */
    val badge: String,
    val host: String,
    val port: Int,
    val tag: String,
    val description: String,
    /** 图标底色，形如 "#4FBF83" */
    val tint: String
) {
    /** 传给启动参数的地址 */
    val address: String
        get() = "$host:$port"
}

/** 内置热门服务器列表，地址于 2026-11 逐一验证在线 */
object HotServers {

    @JvmField
    val LIST: List<HotServer> = listOf(
        HotServer(
            name = "Hypixel",
            badge = "HP",
            host = "mc.hypixel.net",
            port = 25565,
            tag = "Minigames",
            description = "最热闹的小游戏网络，支持 1.8 ~ 最新版本",
            tint = "#E0A21B"
        ),
        HotServer(
            name = "Wynncraft",
            badge = "WY",
            host = "play.wynncraft.com",
            port = 25565,
            tag = "MMORPG",
            description = "开放世界 RPG，自带任务、副本与职业系统",
            tint = "#3FA97C"
        ),
        HotServer(
            name = "CubeCraft",
            badge = "CC",
            host = "play.cubecraft.net",
            port = 25565,
            tag = "Minigames",
            description = "EggWars、SkyWars 等经典小游戏",
            tint = "#2C7DF0"
        ),
        HotServer(
            name = "2b2t",
            badge = "2B",
            host = "2b2t.org",
            port = 25565,
            tag = "Anarchy",
            description = "运营最久远的无规则无政府服务器",
            tint = "#B9302C"
        ),
        HotServer(
            name = "Purple Prison",
            badge = "PP",
            host = "purpleprison.net",
            port = 25565,
            tag = "Prison",
            description = "长期运营的监狱服，矿工与 PvP 玩法",
            tint = "#8B4FD8"
        ),
        HotServer(
            name = "ManaCube",
            badge = "MC",
            host = "play.manacube.com",
            port = 25565,
            tag = "Survival",
            description = "生存、空岛与跑酷等多个长期存档",
            tint = "#2FA6B8"
        ),
        HotServer(
            name = "PikaNetwork",
            badge = "PN",
            host = "play.pika-network.net",
            port = 25565,
            tag = "PvP",
            description = "练习对战与 KitPvP，低延迟客户端友好",
            tint = "#C9A51B"
        ),
        HotServer(
            name = "Skyblock.net",
            badge = "SB",
            host = "play.skyblock.net",
            port = 25565,
            tag = "SkyBlock",
            description = "传统空岛生存，适合长期单岛经营",
            tint = "#5FA8E8"
        )
    )
}
