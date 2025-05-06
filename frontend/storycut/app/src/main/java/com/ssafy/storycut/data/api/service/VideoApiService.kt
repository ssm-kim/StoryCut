package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.VideoListDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface VideoApiService {

    // 내 비디오 목록 조회
    @GET("/api/video/member")
    suspend fun getMyVideos(@Header("Authorization") token: String): Response<BaseResponse<List<VideoListDto>>>

}