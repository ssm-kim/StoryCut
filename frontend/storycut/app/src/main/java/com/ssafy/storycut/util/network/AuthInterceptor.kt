package com.ssafy.storycut.util.network

import android.util.Log
import com.ssafy.storycut.data.api.RetrofitClient
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthInterceptor"

/**
 * 인증 토큰을 요청 헤더에 추가하고, 401 응답 시 토큰 갱신을 처리하는 인터셉터
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository // 인증 리포지토리 주입
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        // 원본 요청 가져오기
        val originalRequest: Request = chain.request()
        
        // 토큰을 추가한 요청 생성
        val requestWithToken = addTokenToRequest(originalRequest)
        
        // 수정된 요청으로 서버 응답 받기
        var response = chain.proceed(requestWithToken)
        
        // 401 응답(토큰 만료) 처리
        if (response.code == 401) {
            Log.d(TAG, "401 Unauthorized 응답 수신. 토큰 갱신 시도...")
            
            // 토큰 갱신 시도
            val newToken = runBlocking { refreshToken() }
            
            // 토큰 갱신 성공 시 원본 요청에 새 토큰 추가하여 재시도
            if (newToken != null) {
                Log.d(TAG, "토큰 갱신 성공. 원본 요청 재시도")
                
                // 기존 응답 바디 닫기
                response.close()
                
                // 새 토큰으로 원본 요청 재시도
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
                    
                return chain.proceed(newRequest)
            } else {
            Log.e(TAG, "토큰 갱신 실패. 인증 오류 응답 반환")
            
            // 토큰 갱신 실패 시 저장된 토큰 삭제
            runBlocking {
                try {
                    tokenManager.clearTokens()
                    Log.d(TAG, "토큰 갱신 실패로 로컬 토큰 삭제 완료")
                } catch (e: Exception) {
                    Log.e(TAG, "토큰 삭제 중 오류 발생", e)
                }
            }
            // 토큰 갱신 실패 시 원본 401 응답 반환
            }
        }
        
        return response
    }
    
    /**
     * 요청에 액세스 토큰을 추가하는 함수
     */
    private fun addTokenToRequest(request: Request): Request {
        val accessToken = runBlocking { tokenManager.accessToken.firstOrNull() } 
        
        return if (!accessToken.isNullOrEmpty()) {
            Log.d(TAG, "요청 헤더에 토큰 추가")
            request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        } else {
            Log.d(TAG, "토큰이 없음, 기본 요청 반환")
            request
        }
    }
    
    /**
     * 토큰 갱신 시도 함수
     * @return 성공 시 새 액세스 토큰, 실패 시 null
     */
    private suspend fun refreshToken(): String? {
        try {
            // 리포지토리를 통해 토큰 갱신
            val tokens = authRepository.refreshAccessToken()
            
            return tokens?.first // 새 액세스 토큰
        } catch (e: Exception) {
            Log.e(TAG, "토큰 갱신 중 예외 발생", e)
            return null
        }
    }
}