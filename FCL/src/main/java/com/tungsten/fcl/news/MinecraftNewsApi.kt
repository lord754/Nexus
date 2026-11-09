package com.tungsten.fcl.news

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/** 拉取官方 Minecraft 新闻的 Retrofit 接口 */
interface MinecraftNewsApi {

    @GET("news.json")
    fun getNews(): Call<MinecraftNews>

    companion object {

        private const val BASE_URL = "https://launchercontent.mojang.com/"

        private val instance: MinecraftNewsApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(MinecraftNewsApi::class.java)
        }

        /** Retrofit 实例只需构建一次，重复调用复用同一实例 */
        @JvmStatic
        fun create(): MinecraftNewsApi = instance
    }
}
