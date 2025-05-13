package com.ssafy.storycut.data.api.model.edit

import com.google.gson.annotations.SerializedName

data class VideoProcessRequest(
    val prompt: String,
    @SerializedName("video_id")
    val videoId: Long,
    val images: List<String> = emptyList(),
    val subtitle: Boolean,
    val musicPrompt: String
)