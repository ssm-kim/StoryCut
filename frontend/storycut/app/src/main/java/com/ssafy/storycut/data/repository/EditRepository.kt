package com.ssafy.storycut.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.ssafy.storycut.data.api.model.VideoDto
import com.ssafy.storycut.data.api.model.edit.VideoProcessRequest
import com.ssafy.storycut.data.api.service.EditService
import com.ssafy.storycut.data.local.datastore.FCMTokenManager
import com.ssafy.storycut.data.local.datastore.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

@Singleton
class EditRepository @Inject constructor(
    private val editService: EditService,
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val fcmTokenManager: FCMTokenManager
) {
    suspend fun uploadVideo(videoUri: Uri, videoTitle: String): Result<Long> {
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

                // 파일이 비어있는지 확인
                if (videoFile.length() == 0L) {
                    return@withContext Result.failure(Exception("비디오 파일이 비어있습니다"))
                }

                // 파일 확장자 확인
                val mimeType = context.contentResolver.getType(videoUri) ?: "video/mp4"

                // MultipartBody.Part 생성 - 필드명을 'file'로 설정 (서버 요구사항에 맞춤)
                val videoRequestBody = videoFile.asRequestBody(mimeType.toMediaTypeOrNull())
                val videoPart = MultipartBody.Part.createFormData("file", videoFile.name, videoRequestBody)

                // video_title Form 데이터 생성
                val titlePart = videoTitle.toRequestBody("text/plain".toMediaTypeOrNull())

                // 로그 추가
                Log.d("EditRepository", "업로드 요청: 파일이름=${videoFile.name}, MIME=$mimeType, 크기=${videoFile.length()}, 제목=$videoTitle")

                // API 호출 - 수정된 파라미터로 호출
                val response = editService.uploadVideo(videoPart, titlePart)

                if (response.isSuccessful) {
                    val baseResponse = response.body()
                    if (baseResponse?.isSuccess == true && baseResponse.result != null) {
                        // VideoDto에서 videoId 추출
                        val videoDto = baseResponse.result
                        Log.d("EditRepository", "업로드 성공")

                        Result.success(videoDto.videoId)
                    } else {
                        Result.failure(Exception("비디오 업로드 실패: ${baseResponse?.message ?: "알 수 없는 오류"}"))
                    }
                } else {
                    // 오류 응답의 바디를 로그에 기록
                    val errorBody = response.errorBody()?.string()
                    Log.e("EditRepository", "서버 오류 응답: ${errorBody ?: "없음"}")

                    Result.failure(Exception("비디오 업로드 실패: ${response.message()}"))
                }
            } catch (e: Exception) {
                Log.e("EditRepository", "비디오 업로드 예외", e)
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
                    // 실제 MIME 타입 가져오기
                    val mimeType = context.contentResolver.getType(imageUri) ?: "image/*"

                    val imageFile = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                        // 파일 확장자를 MIME 타입에서 추출
                        val extension = when (mimeType) {
                            "image/jpeg", "image/jpg" -> ".jpg"
                            "image/png" -> ".png"
                            "image/gif" -> ".gif"
                            "image/webp" -> ".webp"
                            "image/heic", "image/heif" -> ".heic"
                            "image/bmp" -> ".bmp"
                            else -> ".tmp" // 기본 확장자
                        }

                        val tempFile = File.createTempFile("image", extension, context.cacheDir)
                        FileOutputStream(tempFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        tempFile
                    } ?: continue

                    // 실제 MIME 타입으로 RequestBody 생성
                    val imageRequestBody = imageFile.asRequestBody(mimeType.toMediaTypeOrNull())
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
                        // ImageUploadResponse 객체에서 URL 리스트를 추출
                        val imageUrls = baseResponse.result.imageUrls
                        Result.success(imageUrls)
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
        videoTitle: String,
        applySubtitle: Boolean,
        musicPrompt: String?,
        autoMusic: Boolean = false
    ): Result<Boolean> {  // VideoDto 대신 Boolean으로 변경 (성공 여부만 반환)
        return withContext(Dispatchers.IO) {
            try {
                // 요청 객체 생성
                val request = VideoProcessRequest(
                    prompt = prompt ?: "",
                    videoId = videoId,
                    images = imageUrls,
                    videoTitle = videoTitle,
                    subtitle = applySubtitle,
                    musicPrompt = musicPrompt ?: "",
                    autoMusic = autoMusic  // autoMusic 필드 추가
                )

                // 인증 토큰 가져오기
                val authToken = "Bearer ${tokenManager.accessToken.first() ?: ""}"

                // FCM 토큰 가져오기
                val deviceToken = getFCMToken()

                // API 호출
                val response = editService.processVideo(authToken, deviceToken, request)

                // 응답 처리 (단순화)
                val baseResponse = response.body()

                if (response.isSuccessful && baseResponse?.isSuccess == true) {
                    Log.d("EditRepository", "영상 처리 요청 성공: ${baseResponse.message}")
                    Result.success(true)
                } else {
                    // 실패 처리
                    val errorMsg = baseResponse?.message ?: response.message() ?: "알 수 없는 오류"
                    Log.e("EditRepository", "영상 처리 요청 실패: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("EditRepository", "영상 처리 예외 발생", e)
                Result.failure(e)
            }
        }
    }

    // FCM 토큰 가져오기 (없으면 새로 요청)
    private suspend fun getFCMToken(): String {
        // 저장된 토큰 확인
        val savedToken = fcmTokenManager.getToken()

        // 토큰이 있으면 바로 반환
        if (savedToken.isNotEmpty()) {
            return savedToken
        }

        // 토큰이 없으면 새로 요청
        return suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    // 토큰 저장
                    fcmTokenManager.saveToken(token)
                    // 결과 반환
                    continuation.resume(token)
                } else {
                    Log.e("EditRepository", "FCM 토큰 가져오기 실패", task.exception)
                    // 빈 문자열 반환
                    continuation.resume("")
                }
            }
        }
    }
}