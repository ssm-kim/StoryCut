package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.GoogleLoginRequest
import com.ssafy.storycut.data.api.model.TokenResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/google/mobile")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): Response<BaseResponse<TokenResult>>
}