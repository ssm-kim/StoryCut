package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.*
import com.ssafy.storycut.data.api.model.edit.ImageUploadResponse
import com.ssafy.storycut.data.api.model.edit.MosaicRequest
import com.ssafy.storycut.data.api.model.edit.VideoProcessRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface EditService {

    // 이미지 업로드 API - 여러 파일 업로드 가능
    @Multipart
    @POST("upload/images")
    suspend fun uploadImages(@Part files: List<MultipartBody.Part>):
            Response<BaseResponse<List<ImageUploadResponse>>>

    // 영상 업로드 API - 단일 파일 업로드
    @Multipart
    @POST("upload/videos")  // 경로 확인 (서버 코드와 일치하는지)
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part,
        @Part("video_title") videoTitle: RequestBody
    ): Response<BaseResponse<VideoDto>>

    // 룸 썸네일 이미지 업로드 API
    @Multipart
    @POST("upload/room-thumbnails")
    suspend fun uploadRoomThumbNailImage(@Part file: MultipartBody.Part):
            Response<BaseResponse<ThumbNailUploadResponse>>

    // 영상 처리 API (요약 + 모자이크 + 자막)
    @POST("videos")
    suspend fun processVideo(
        @Header("authorization") authorization: String,
        @Header("device-token") deviceToken: String,
        @Body request: VideoProcessRequest
    ): Response<BaseResponse<VideoDto>>

    // 모자이크 API
    @POST("mosaic")
    suspend fun applyMosaic(
        @Header("authorization") authorization: String,
        @Header("device-token") deviceToken: String,
        @Body request: MosaicRequest
    ): Response<BaseResponse<VideoDto>>
}