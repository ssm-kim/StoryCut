package com.ssafy.storycut.data.repository

import android.util.Log
import com.ssafy.storycut.data.api.RetrofitClient
import com.ssafy.storycut.data.api.model.GoogleLoginRequest
import com.ssafy.storycut.data.local.datastore.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
    private val tokenManager: TokenManager
) {

    /**
     * 구글 로그인 토큰을 서버로 전송하는 함수
     * @param idToken 구글 ID 토큰
     * @param onComplete 완료 시 호출되는 콜백 (결과 메시지, 토큰 쌍)
     */
    suspend fun sendTokenToServer(
        idToken: String,
        onComplete: (String, Pair<String, String>?) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.authService.googleLogin(GoogleLoginRequest(idToken))

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.isSuccess) {
                        // 로그인 성공, 응답 데이터 처리
                        val result = responseBody.result
                        if (result != null) {
                            val accessToken = result.accessToken
                            val refreshToken = result.refreshToken

                            // 토큰 저장
                            tokenManager.saveTokens(accessToken, refreshToken)

                            // 토큰 정보 반환
                            val tokenPair = Pair(accessToken, refreshToken)

                            withContext(Dispatchers.Main) {
                                onComplete("성공", tokenPair)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onComplete("성공했지만 토큰이 없습니다.", null)
                            }
                        }
                    } else {
                        // 서버에서 오류 응답
                        val errorMessage = responseBody?.message ?: "알 수 없는 오류"
                        Log.e(TAG, "서버 응답 오류: $errorMessage")
                        withContext(Dispatchers.Main) {
                            onComplete("실패: $errorMessage", null)
                        }
                    }
                } else {
                    // HTTP 오류
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "HTTP 오류 응답: ${response.code()} - $errorBody")
                    withContext(Dispatchers.Main) {
                        onComplete("실패: HTTP ${response.code()}", null)
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 등
                Log.e(TAG, "토큰 전송 중 예외 발생", e)
                withContext(Dispatchers.Main) {
                    onComplete("실패: ${e.message}", null)
                }
            }
        }
    }
}