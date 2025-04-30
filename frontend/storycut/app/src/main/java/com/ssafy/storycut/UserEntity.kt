package com.ssafy.storycut

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val authCode: String? = null,
    val lastLoginTime: Long = System.currentTimeMillis() // 마지막 로그인 시간 추가
)