package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.GoogleLoginRequest
import com.ssafy.storycut.data.api.model.TokenResult
import com.ssafy.storycut.data.api.model.UserInfo
import com.ssafy.storycut.data.api.model.credential.GooglePermissionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {
    @POST("auth/login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<BaseResponse<TokenResult>>

    @GET("members/detail")
    suspend fun getUserInfo(@Header("Authorization") authorization: String): Response<BaseResponse<UserInfo>>

    // 특정 회원 정보 조회 API 추가
    @GET("members/{memberId}")
    suspend fun getMemberById(
        @Path("memberId") memberId: Long
    ): Response<BaseResponse<UserInfo>>

    // 토큰 갱신 API 추가
    @POST("auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<BaseResponse<TokenResult>>

    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<Unit>>

    @GET("auth/youtube/auth")
    suspend fun getYouTubeAuthUrl(@Header("Authorization") authorization: String): Response<BaseResponse<GooglePermissionResponse>>

    @POST("auth/google-refresh")
    suspend fun refreshGoogleToken(@Header("Authorization") authorization: String): Response<BaseResponse<TokenResult>>

}