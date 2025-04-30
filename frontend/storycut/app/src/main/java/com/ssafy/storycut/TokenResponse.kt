package com.ssafy.storycut

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long, // 액세스 토큰 만료 시간(초)
    val tokenType: String = "Bearer"
)
