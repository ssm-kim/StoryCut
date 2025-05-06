package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.GoogleLoginRequest
import com.ssafy.storycut.data.api.model.TokenResult
import com.ssafy.storycut.data.api.model.UserInfo
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<BaseResponse<TokenResult>>
    
    @GET("api/members/detail")
    suspend fun getUserInfo(@Header("Authorization") authorization: String): Response<BaseResponse<UserInfo>>
    
    // 토큰 갱신 API 추가
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): Response<BaseResponse<TokenResult>>

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") authorization: String): Response<BaseResponse<Unit>>
}