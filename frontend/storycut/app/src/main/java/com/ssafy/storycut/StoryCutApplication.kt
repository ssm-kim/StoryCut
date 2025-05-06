package com.ssafy.storycut

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StoryCutApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 로그 필터 설정
        setupLogFilters()
    }
    
    private fun setupLogFilters() {
        // OkHttp 관련 로그 필터링
        val filterOkHttp = { tag: String, _: Int ->
            if (tag.contains("okhttp", ignoreCase = true) ||
                tag.contains("OkHttp", ignoreCase = true) ||
                tag.contains("Retrofit", ignoreCase = true)) {
                Log.ASSERT // 로그 표시 안함 (가장 높은 레벨로 설정)
            } else {
                Log.VERBOSE // 모든 로그 표시 (가장 낮은 레벨)
            }
        }
        
        try {
            Log.i("LogFilter", "Log filter set up")
        } catch (e: Exception) {
            Log.e("LogFilter", "Failed to set up log filter", e)
        }
    }
}