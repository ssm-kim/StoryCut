package com.ssafy.storycut.data.api.model.room

data class RoomDto(
    val roomId: Long,
    val hostId: Long,
    val roomTitle: String,
    val hasPassword: Boolean,
    val roomThumbnail: String?,
    val createdAt: String,
    val updatedAt: String,
    val memberCount: Int
)