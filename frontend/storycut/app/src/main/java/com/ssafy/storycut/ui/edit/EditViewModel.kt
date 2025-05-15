package com.ssafy.storycut.ui.edit

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.VideoDto
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.EditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repository: EditRepository,
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager
) : ViewModel() {
    // UI 상태 관련 변수들
    var videoSelected by mutableStateOf(false)
        private set
    var selectedVideoUri by mutableStateOf<Uri?>(null)
        private set
    var videoThumbnail by mutableStateOf<Bitmap?>(null)
        private set
    var hasMosaic by mutableStateOf(false)
        private set
    var applySubtitle by mutableStateOf(false)
        private set
    var hasBackgroundMusic by mutableStateOf(false)
        private set
    var promptText by mutableStateOf("")
        private set
    // 배경음악을 위한 별도의 프롬프트 변수 추가
    var musicPromptText by mutableStateOf("")
        private set
    var mosaicImages by mutableStateOf<List<Uri>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var videoTitle by mutableStateOf("")
        private set

    fun updateVideoTitle(title: String) {
        videoTitle = title
    }

    // 이벤트 플로우
    private val _events = MutableSharedFlow<EditEvent>()
    val events: SharedFlow<EditEvent> = _events.asSharedFlow()

    // 비디오 선택
    fun setSelectedVideo(uri: Uri) {
        selectedVideoUri = uri
        videoSelected = true
        loadVideoThumbnail(uri)
    }

    // 비디오 썸네일 로드
    private fun loadVideoThumbnail(uri: Uri) {
        viewModelScope.launch {
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, uri)
                videoThumbnail = retriever.getFrameAtTime(0)
                retriever.release()
            } catch (e: Exception) {
                Log.e("EditViewModel", "썸네일 로드 실패", e)
                videoThumbnail = null
            }
        }
    }

    // 모자이크 옵션 토글
    fun toggleMosaic(enable: Boolean) {
        hasMosaic = enable
        if (!enable) {
            mosaicImages = emptyList()
        }
    }

    // 한국어 자막 옵션 토글
    fun toggleKoreanSubtitle(enable: Boolean) {
        applySubtitle = enable
    }

    // 배경 음악 옵션 토글
    fun toggleBackgroundMusic(enable: Boolean) {
        hasBackgroundMusic = enable
    }

    // 프롬프트 텍스트 업데이트
    fun updatePromptText(text: String) {
        promptText = text
    }

    // 배경음악 프롬프트 텍스트 업데이트 함수 추가
    fun updateMusicPromptText(text: String) {
        musicPromptText = text
    }

    // 모자이크 이미지 추가
    fun addMosaicImage(uri: Uri) {
        if (mosaicImages.size < 2) {
            mosaicImages = mosaicImages + uri
        }
    }

    // 모자이크 이미지 제거
    fun removeMosaicImage(index: Int) {
        mosaicImages = mosaicImages.filterIndexed { i, _ -> i != index }
    }

    // 오류 메시지 초기화
    fun clearError() {
        error = null
    }

    // 편집 처리 시작
    fun processEditing() {
        if (selectedVideoUri == null) {
            error = "비디오를 선택해주세요"
            return
        }
        if (videoTitle.isBlank()) {
            error = "비디오 제목을 입력해주세요"
            return
        }

        isLoading = true
        error = null

        viewModelScope.launch {
            try {
                // 1. 비디오 업로드
                val videoUploadResult = repository.uploadVideo(selectedVideoUri!!, videoTitle)
                if (videoUploadResult.isFailure) {
                    throw videoUploadResult.exceptionOrNull() ?: Exception("비디오 업로드 실패")
                }

                val videoId = videoUploadResult.getOrNull() ?: throw Exception("업로드된 비디오 ID를 찾을 수 없습니다")

                // 2. 모자이크 이미지 업로드 (필요시)
                var imageUrls = listOf<String>()
                if (hasMosaic && mosaicImages.isNotEmpty()) {
                    val imageUploadResult = repository.uploadImages(mosaicImages)
                    if (imageUploadResult.isSuccess) {
                        imageUrls = imageUploadResult.getOrNull() ?: emptyList()
                    }
                }

                // 4. 비디오 처리 요청 - 배경 음악 프롬프트와 일반 프롬프트 분리
                val processResult = repository.processVideo(
                    prompt = promptText.takeIf { it.isNotBlank() },
                    videoId = videoId,
                    imageUrls = imageUrls,
                    videoTitle = videoTitle,
                    applySubtitle = applySubtitle,
                    musicPrompt = if (hasBackgroundMusic) musicPromptText.takeIf { it.isNotBlank() } else null
                )

                isLoading = false

                if (processResult.isSuccess) {
                    val processedVideo = processResult.getOrNull()
                    if (processedVideo != null) {
                        _events.emit(EditEvent.Success(processedVideo.videoId.toString(), processedVideo))
                    } else {
                        error = "처리된 비디오 정보를 가져올 수 없습니다"
                    }
                } else {
                    throw processResult.exceptionOrNull() ?: Exception("영상 처리 실패")
                }

            } catch (e: Exception) {
                Log.e("EditViewModel", "영상 처리 실패", e)
                isLoading = false
                error = e.message ?: "오류가 발생했습니다"
                _events.emit(EditEvent.Error(error ?: "알 수 없는 오류"))
            }
        }
    }

    // 이벤트 클래스
    sealed class EditEvent {
        data class Success(val videoId: String, val videoDto: VideoDto) : EditEvent()
        data class Error(val message: String) : EditEvent()
    }
}