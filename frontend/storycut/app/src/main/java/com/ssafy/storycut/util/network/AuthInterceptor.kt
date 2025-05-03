package com.ssafy.storycut.util.network

import android.util.Log
import com.ssafy.storycut.data.local.datastore.TokenManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking // Interceptor 내 동기 처리를 위해 사용 (주의 필요)
import okhttp3.Interceptor // Interceptor 인터페이스 import
import okhttp3.Response
import okhttp3.Request // Request 클래스 import
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AuthInterceptor"

/**
 * 인증 토큰을 요청 헤더에 추가하고, 401 응답 시 토큰 갱신을 처리하는 인터셉터 (401 처리 로직 추가 필요)
 */
// Interceptor 인터페이스를 구현하도록 클래스 선언 수정
@Singleton
class AuthInterceptor @Inject constructor(
    // Hilt를 사용하여 TokenManager를 생성자로 직접 주입받도록 수정
    private val tokenManager: TokenManager
) : Interceptor { // Interceptor 구현 선언 추가

    // @Inject lateinit var tokenManager: TokenManager // 생성자 주입 사용하므로 제거

    @Throws(IOException::class)
    // operator fun invoke 대신 Interceptor 인터페이스의 필수 메서드인 intercept 구현
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request() // 원본 요청 가져오기
        var accessToken: String? = null

        // --------------- 주의: runBlocking 사용은 Interceptor에서 권장되지 않습니다. ---------------
        // DataStore Flow에서 토큰을 가져오기 위해 일시적으로 runBlocking 사용.
        // 실제 프로덕션에서는 TokenManager 내부에 동기적으로 접근 가능한 캐시를 두거나,
        // 다른 비동기 처리 패턴을 고려해야 합니다.
        // 여기서는 예시를 위해 사용합니다.
        // -----------------------------------------------------------------------------
        try {
            // runBlocking을 사용하여 DataStore에서 액세스 토큰을 동기적으로 가져옴
            accessToken = runBlocking { tokenManager.accessToken.firstOrNull() }
            Log.d(TAG, "가져온 액세스 토큰: $accessToken")
        } catch (e: Exception) {
            Log.e(TAG, "액세스 토큰 조회 중 오류", e)
            // 토큰 조회 실패 시 로그아웃 등 추가 오류 처리 로직 필요
        }

        // 새 요청 빌더 생성 (원본 요청 기반)
        val requestBuilder = originalRequest.newBuilder()

        // 액세스 토큰이 존재하면 Authorization 헤더에 추가
        if (!accessToken.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
            Log.d(TAG, "요청 헤더에 토큰 추가")
        } else {
            Log.d(TAG, "액세스 토큰이 없어 헤더에 추가하지 않음")
        }

        // 수정된 (토큰이 추가된) 요청 생성
        val request = requestBuilder.build()

        // 수정된 요청을 진행하고 서버 응답을 받음
        val response = chain.proceed(request)

        // --------------- 중요: 여기서 401 Unauthorized 응답을 처리해야 합니다. ---------------
        // 서버에서 401 응답을 받았다면, 토큰 갱신 로직을 수행하고
        // 갱신 성공 시 원래 요청을 새 토큰으로 재시도해야 합니다.

        // 예시 401 처리 흐름 (구현 필요):
        // if (response.code == 401) {
        //    Log.d(TAG, "401 Unauthorized 응답 수신. 토큰 갱신 시도...")
        //    // 1. TokenManager에서 리프레시 토큰 가져오기 (runBlocking 필요할 수 있음 - 주의!)
        //    // 2. 별도의 OkHttp 클라이언트(이 Interceptor가 포함되지 않은)를 사용하여 토큰 갱신 API 호출
        //    // 3. 갱신 성공 시, TokenManager.saveTokens()로 새 토큰 저장 (runBlocking 필요할 수 있음 - 주의!)
        //    // 4. 원래 요청 (originalRequest)을 새 액세스 토큰으로 다시 만들어 재시도:
        //    //    val refreshedAccessToken = runBlocking { tokenManager.accessToken.first() } // 새로 저장된 토큰 가져옴
        //    //    val newRequestWithRefreshedToken = originalRequest.newBuilder()
        //    //        .header("Authorization", "Bearer $refreshedAccessToken")
        //    //        .build()
        //    //    // 기존 401 응답 바디 닫기 (필요하다면)
        //    //    response.close()
        //    //    // 새 요청으로 체인 다시 진행
        //    //    return chain.proceed(newRequestWithRefreshedToken)
        //    // 5. 갱신 실패 시 (리프레시 토큰 만료 등), 사용자 로그아웃 처리 (예: TokenManager.clearTokens() 호출 후 로그인 화면 이동)
        // }
        // -------------------------------------------------------------------------


        // 401 에러 처리가 여기에 추가되어야 최종적인 인증 Interceptor가 됩니다.
        // 현재는 단순히 요청에 토큰을 추가하고 응답을 반환하는 기본 기능만 포함합니다.

        return response // 최종 응답 반환 (401 처리 로직에 따라 달라질 수 있음)
    }

    // operator fun invoke 함수는 Interceptor 인터페이스 구현 시 필요 없습니다. 제거합니다.
    // @Throws(IOException::class)
    // operator fun invoke(chain: Interceptor.Chain): Response { ... }
}