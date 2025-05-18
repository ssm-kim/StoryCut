package com.ssafy.storycut.data.api.model

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,

    val googleAccessToken: String
)