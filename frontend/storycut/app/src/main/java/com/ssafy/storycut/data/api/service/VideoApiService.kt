package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.VideoDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface VideoApiService {

    // 내 비디오 목록 조회
    @GET("/api/video/member")
    suspend fun getMyVideos(@Header("Authorization") token: String): Response<BaseResponse<List<VideoDto>>>

    @GET("/api/video/{videoId}")
    suspend fun getMyVideo(@Path("videoId") videoId: String, @Header("Authorization") token: String
    ): Response<BaseResponse<VideoDto>>

    @GET("/api/video/room/{roomId}")
    suspend fun getRoomVideos(@Path("roomId") roomId: String, @Header("Authorization") token: String): Response<BaseResponse<List<VideoDto>>>
}