package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.*
import com.ssafy.storycut.data.api.model.edit.ImageUploadResponse
import com.ssafy.storycut.data.api.model.edit.MosaicRequest
import com.ssafy.storycut.data.api.model.edit.VideoProcessRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface S3Service {

    // 이미지 업로드 API - 여러 파일 업로드 가능
    @Multipart
    @POST("/api/upload/images")
    suspend fun uploadImages(@Part files: List<MultipartBody.Part>):
            Response<BaseResponse<ImageUploadResponse>>

    // 영상 업로드 API - 단일 파일 업로드
    @Multipart
    @POST("/api/upload/videos")
    suspend fun uploadVideo(@Part file: MultipartBody.Part):
            Response<BaseResponse<ImageUploadResponse>>

    // 룸 썸네일 이미지 업로드 API
    @Multipart
    @POST("/api/upload/room-thumbnails")
    suspend fun uploadRoomThumbNailImage(@Part file: MultipartBody.Part):
            Response<BaseResponse<ThumbNailUploadResponse>>

    // 영상 처리 API (요약 + 모자이크 + 자막)
    @POST("/api/videos")
    suspend fun processVideo(
        @Header("authorization") authorization: String,
        @Body request: VideoProcessRequest
    ): Response<BaseResponse<VideoDto>>

    // 모자이크 API
    @POST("/api/mosaic")
    suspend fun applyMosaic(
        @Header("authorization") authorization: String, @Body request: MosaicRequest
    ): Response<BaseResponse<VideoDto>>
}