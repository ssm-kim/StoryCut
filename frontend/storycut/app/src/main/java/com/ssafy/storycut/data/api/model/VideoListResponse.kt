package com.ssafy.storycut.data.api.model

data class VideoListDto(
    val videoId: Long,
    val memberId: Long,
    val videoName: String,
    val videoUrl: String,
    val thumbnail: String,
    val originalVideoId: Long,
    val createdAt: String, // ISO 형식 (String → 필요시 LocalDateTime으로 파싱)
    val updatedAt: String
)