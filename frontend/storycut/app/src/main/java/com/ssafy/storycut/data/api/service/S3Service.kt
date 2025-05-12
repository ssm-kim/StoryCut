package com.ssafy.storycut.data.api.service

import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.ImageUrlResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface S3Service {

    @Multipart
    @POST("/api/upload/images")
    suspend fun uploadRoomThumbNailImage(@Part image: MultipartBody.Part): Response<BaseResponse<ImageUrlResponse>>

}