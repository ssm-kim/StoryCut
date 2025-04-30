package com.ssafy.storycut.feature.auth.domain

data class AuthUser(
    val id: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean = false
)