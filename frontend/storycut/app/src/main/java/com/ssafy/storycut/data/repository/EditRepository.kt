package com.ssafy.storycut.data.repository

import android.content.Context
import android.net.Uri
import com.ssafy.storycut.data.api.model.VideoDto
import com.ssafy.storycut.data.api.model.edit.ImageUploadResponse
import com.ssafy.storycut.data.api.model.edit.VideoProcessRequest
import com.ssafy.storycut.data.api.service.EditService
import com.ssafy.storycut.data.local.datastore.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Singleton
class EditRepository @Inject constructor(
    private val editService: EditService,
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager
) {
    suspend fun uploadVideo(videoUri: Uri): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                // URI에서 파일 생성
                val videoFile = context.contentResolver.openInputStream(videoUri)?.use { inputStream ->
                    val tempFile = File.createTempFile("video", ".mp4", context.cacheDir)
                    FileOutputStream(tempFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    tempFile
                } ?: throw Exception("비디오 파일을 처리할 수 없습니다")

                // MultipartBody.Part 생성
                val videoRequestBody = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
                val videoPart = MultipartBody.Part.createFormData("file", videoFile.name, videoRequestBody)

                // API 호출
                val response = editService.uploadVideo(videoPart)

                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.isSuccess == true && baseResponse.result != null && baseResponse.result.isNotEmpty()) {
                        // 첫 번째 응답의 URL에서 비디오 ID 추출 (실제 구현은 서버 응답에 따라 다를 수 있음)
                        // 예시로 1L 반환
                        Result.success(1L)
                    } else {
                        Result.failure(Exception("비디오 업로드 실패: ${baseResponse?.message ?: "알 수 없는 오류"}"))
                    }
                } else {
                    Result.failure(Exception("비디오 업로드 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun uploadImages(imageUris: List<Uri>): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val imageParts = mutableListOf<MultipartBody.Part>()

                // 각 URI에서 파일 생성 및 MultipartBody.Part 추가
                for (imageUri in imageUris) {
                    val imageFile = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        val tempFile = File.createTempFile("image", ".jpg", context.cacheDir)
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        tempFile
                    } ?: continue

                    val imageRequestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("files", imageFile.name, imageRequestBody)
                    imageParts.add(imagePart)
                }

                if (imageParts.isEmpty()) {
                    throw Exception("처리할 이미지가 없습니다")
                }

                // API 호출
                val response = editService.uploadImages(imageParts)

                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.isSuccess == true && baseResponse.result != null) {
                        // 응답에서 이미지 URL 목록 추출
                        val allImageUrls = baseResponse.result.map { it.imageUrls }
                        Result.success(allImageUrls)
                    } else {
                        Result.failure(Exception("이미지 업로드 실패: ${baseResponse?.message ?: "알 수 없는 오류"}"))
                    }
                } else {
                    Result.failure(Exception("이미지 업로드 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun processVideo(
        prompt: String?,
        videoId: Long,
        imageUrls: List<String>,
        generateSubtitles: Boolean,
        musicPrompt: String?
    ): Result<VideoDto> {
        return withContext(Dispatchers.IO) {
            try {
                // API 요청 문서에 맞게 VideoProcessRequest 객체 생성
                val request = VideoProcessRequest(
                    prompt = prompt ?: "",
                    videoId = videoId,
                    images = imageUrls,
                    subtitle = generateSubtitles,
                    musicPrompt = musicPrompt ?: ""
                )

                // 토큰 가져오기
                val token = tokenManager.accessToken.first() ?: ""
                val response = editService.processVideo("Bearer $token", request)

                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.isSuccess == true && baseResponse.result != null) {
                        Result.success(baseResponse.result)
                    } else {
                        Result.failure(Exception("영상 처리 실패: ${baseResponse?.message ?: "알 수 없는 오류"}"))
                    }
                } else {
                    Result.failure(Exception("영상 처리 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}