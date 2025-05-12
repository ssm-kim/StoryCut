package com.ssafy.storycut.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log // 로깅용 추가
import com.ssafy.storycut.data.api.RetrofitClient
import com.ssafy.storycut.data.api.model.BaseResponse
import com.ssafy.storycut.data.api.model.ImageUrlResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class S3Repository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // URI에서 실제 파일 생성
    private fun getFileFromUri(uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")

        inputStream?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    suspend fun uploadRoomThumbNailImage(imageUri: Uri): Response<BaseResponse<ImageUrlResponse>> {
        try {
            Log.d("S3Repository", "Starting upload with URI: $imageUri")
            val file = getFileFromUri(imageUri)
            Log.d("S3Repository", "File created: ${file.absolutePath}, Size: ${file.length()}")

            // MultipartBody.Part 생성
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            // 여기서 "file"을 "files"로 변경
            val imagePart = MultipartBody.Part.createFormData("files", file.name, requestFile)
            Log.d("S3Repository", "Created part with name 'files' and filename '${file.name}'")

            // S3 업로드 API 호출
            val response = RetrofitClient.s3Service.uploadRoomThumbNailImage(imagePart)
            Log.d("S3Repository", "Response: ${response.code()}, ${response.message()}")

            if (!response.isSuccessful) {
                Log.e("S3Repository", "Error body: ${response.errorBody()?.string()}")
            }

            return response
        } catch (e: Exception) {
            Log.e("S3Repository", "Exception during upload: ${e.message}", e)
            throw e
        }
    }
}