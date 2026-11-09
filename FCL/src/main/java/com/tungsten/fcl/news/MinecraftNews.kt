package com.tungsten.fcl.news

import com.google.gson.annotations.SerializedName

/**
 * https://launchercontent.mojang.com/news.json 的响应体。
 * 启动器只关心其中的官方新闻条目列表。
 */
data class MinecraftNews(
    @SerializedName("version")
    val version: Int = 0,
    @SerializedName("entries")
    val entries: List<MinecraftNewsEntry> = emptyList()
)

/** news.json entries 数组中的一条官方 Minecraft 新闻 */
data class MinecraftNewsEntry(
    @SerializedName("title")
    val title: String = "",
    @SerializedName("category")
    val category: String = "",
    @SerializedName("date")
    val date: String = "",
    @SerializedName("text")
    val text: String = "",
    @SerializedName("readMoreLink")
    val readMoreLink: String = ""
)
