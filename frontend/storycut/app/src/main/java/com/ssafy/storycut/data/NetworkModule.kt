package com.ssafy.storycut.common.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ssafy.storycut.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private const val BASE_URL = BuildConfig.BASE_URL

    private val gson: Gson by lazy {
        GsonBuilder().setLenient().create()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}