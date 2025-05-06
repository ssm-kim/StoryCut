package com.ssafy.storycut.data.repository

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.VideoListDto
import com.ssafy.storycut.data.api.service.VideoApiService
import retrofit2.Response
import javax.inject.Inject

class VideoRepository @Inject constructor(
    private val videoApiService: VideoApiService
) {
    suspend fun getMyVideos(token: String): Response<BaseResponse<List<VideoListDto>>> {
        return videoApiService.getMyVideos("Bearer $token")
    }
}