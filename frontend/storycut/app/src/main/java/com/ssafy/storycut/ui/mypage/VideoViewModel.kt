package com.ssafy.storycut.ui.mypage

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.VideoDto
import com.ssafy.storycut.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _myVideos = MutableStateFlow<List<VideoDto>>(emptyList())
    val myVideos: StateFlow<List<VideoDto>> = _myVideos

    // 비디오 상세 정보를 위한 StateFlow 추가
    private val _videoDetail = MutableStateFlow<VideoDto?>(null)
    val videoDetail: StateFlow<VideoDto?> = _videoDetail

    fun fetchMyVideos(token: String) {
        viewModelScope.launch {
            try {
                val response = videoRepository.getMyVideos(token)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result?.let {
                        _myVideos.value = it
                    }
                } else {
                    // 에러 처리 - 로그 추가
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.d("VideoViewModel", "비디오 목록 가져오기 실패 : ${errorMsg}")
                }
            } catch (e: Exception) {
                // 예외 처리 - 로그 추가
                Log.d("VideoViewModel", "비디오 목록 가져오기 에러 : ${e.message}")
            }
        }
    }

    // 매개변수로 token을 받도록 수정
    suspend fun getVideoDetail(videoId: String, token: String): VideoDto? {
        try {
            val response = videoRepository.getVideoDetail(videoId, token)

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                val videoDto = response.body()?.result
                _videoDetail.value = videoDto
                return videoDto
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                Log.d("VideoViewModel", "비디오 상세 정보 가져오기 실패 : ${errorMsg}")
            }
        } catch (e: Exception) {
            Log.d("VideoViewModel", "비디오 상세 정보 가져오기 에러 : ${e.message}")
        }
        return null
    }

    // 비동기적으로 비디오 상세 정보를 가져오는 래퍼 함수 (ViewModelScope 사용)
    fun fetchVideoDetail(videoId: String, token: String) {
        viewModelScope.launch {
            try {
                getVideoDetail(videoId, token)
            } catch (e: Exception) {
                Log.d("VideoViewModel", "비디오 상세 정보 로드 에러: ${e.message}")
            }
        }
    }

    // 로컬 캐시에서 비디오 정보 찾기 (API 요청 없이 빠르게 정보 제공)
    fun getVideoFromCache(videoId: String): VideoDto? {
        // videoId를 Long으로 변환하여 비교
        val videoIdLong = videoId.toLongOrNull()
        return if (videoIdLong != null) {
            myVideos.value.find { it.videoId == videoIdLong }
        } else {
            // 변환할 수 없는 경우 null 반환
            null
        }
    }
}
