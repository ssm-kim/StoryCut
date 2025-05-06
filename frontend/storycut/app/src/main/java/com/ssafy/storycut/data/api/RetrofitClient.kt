package com.ssafy.storycut.data.api

import com.google.gson.GsonBuilder
import com.ssafy.storycut.BuildConfig
import com.ssafy.storycut.data.api.service.AuthApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = BuildConfig.BASE_URL

object RetrofitClient {
    // 로깅 인터셉터 설정 - 개발 빌드에서만 BODY 레벨, 릴리스에서는 NONE
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // BuildConfig.DEBUG가 true일 때만 로깅 활성화
        // 로그를 완전히 끄려면 NONE으로 설정
        level = HttpLoggingInterceptor.Level.NONE
    }

    // OkHttpClient 설정
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Gson 인스턴스 생성 (lenient 모드 활성화)
    private val gson = GsonBuilder()
        .setLenient() // 잘못된 JSON 형식 처리를 위한 설정
        .create()

    // Retrofit 인스턴스 생성
    val authService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)) // 커스텀 Gson 인스턴스 사용
            .build()
            .create(AuthApiService::class.java)
    }
}