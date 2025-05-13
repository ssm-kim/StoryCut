package com.ssafy.storycut.data.api.model.edit

// 비디오 처리 요청 모델
data class VideoProcessRequest(
    val prompt: String,
    val video_id: Long,
    val images: List<String>,
    val subtitle: Boolean,
    val musicPrompt: String
)