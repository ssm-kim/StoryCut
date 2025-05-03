package com.ssafy.storycut.data.api.model

/**
 * 사용자 정보 모델
 */
data class UserInfo(
    val userId: String,
    val email: String,
    val name: String,
    val profileImageUrl: String?
)