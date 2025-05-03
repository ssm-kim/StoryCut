package com.ssafy.storycut.data.api.model

data class BaseResponse<T>(
    val isSuccess: Boolean,
    val code: Long,
    val message: String,
    val result: T? = null
)

data class GoogleLoginRequest(val idToken: String)

data class TokenResult(
    val accessToken: String,
    val refreshToken: String
)