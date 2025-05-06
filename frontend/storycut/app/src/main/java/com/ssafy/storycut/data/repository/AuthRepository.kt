package com.ssafy.storycut.data.repository

import android.util.Log
import com.ssafy.storycut.data.api.RetrofitClient
import com.ssafy.storycut.data.api.model.GoogleLoginRequest
import com.ssafy.storycut.data.api.model.UserInfo
import com.ssafy.storycut.data.local.datastore.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthRepository"

@Singleton
class AuthRepository @Inject constructor(
    private val tokenManager: TokenManager
) {
    // 현재 로그인한 사용자 정보를 저장
    private var currentUser: UserInfo? = null

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

                            // 토큰이 저장되면 사용자 정보 가져오기
                            val userInfoResult = getUserInfo()
                            
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
    
    /**
     * 사용자 정보를 가져오는 함수
     * @return 성공 시 UserInfo, 실패 시 null
     */
    suspend fun getUserInfo(): UserInfo? {
        return withContext(Dispatchers.IO) {
            try {
                // 저장된 토큰 가져오기
                val token = tokenManager.accessToken.first()
                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "토큰이 없습니다.")
                    return@withContext null
                }
                
                val authHeader = "Bearer $token"
                val response = RetrofitClient.authService.getUserInfo(authHeader)
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.isSuccess) {
                        // 사용자 정보를 가져오는데 성공
                        val userInfo = responseBody.result
                        if (userInfo != null) {
                            // 사용자 정보 저장
                            currentUser = userInfo
                            return@withContext userInfo
                        }
                    }
                    Log.e(TAG, "사용자 정보를 가져오는데 실패: ${responseBody?.message}")
                } else {
                    // HTTP 오류
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "사용자 정보 HTTP 오류: ${response.code()} - $errorBody")
                }
                
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "사용자 정보를 가져오는 중 예외 발생", e)
                return@withContext null
            }
        }
    }
    
    /**
     * 리프레시 토큰을 사용하여 액세스 토큰을 갱신하는 함수
     * @return 성공 시 토큰 쌍, 실패 시 null
     */
    suspend fun refreshAccessToken(): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            try {
                // 저장된 리프레시 토큰 가져오기
                val refreshToken = tokenManager.refreshToken.first()
                if (refreshToken.isNullOrEmpty()) {
                    Log.e(TAG, "리프레시 토큰이 없습니다.")
                    return@withContext null
                }
                
                // 리프레시 토큰 헤더 설정
                val authHeader = "Bearer $refreshToken"
                val response = RetrofitClient.authService.refreshToken(authHeader)
                
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && responseBody.isSuccess) {
                        // 토큰 갱신 성공
                        val result = responseBody.result
                        if (result != null) {
                            val newAccessToken = result.accessToken
                            val newRefreshToken = result.refreshToken
                            
                            // 새 토큰 저장
                            tokenManager.saveTokens(newAccessToken, newRefreshToken)
                            Log.d(TAG, "토큰 갱신 성공")
                            
                            return@withContext Pair(newAccessToken, newRefreshToken)
                        }
                    }
                    Log.e(TAG, "토큰 갱신 실패: ${responseBody?.message}")
                } else {
                    // HTTP 오류
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "토큰 갱신 HTTP 오류: ${response.code()} - $errorBody")
                }
                
                return@withContext null
            } catch (e: Exception) {
                Log.e(TAG, "토큰 갱신 중 예외 발생", e)
                return@withContext null
            }
        }
    }
    
    /**
     * 현재 로그인한 사용자 정보 반환
     * @return 사용자 정보
     */
    fun getCurrentUser(): UserInfo? {
        return currentUser
    }
}