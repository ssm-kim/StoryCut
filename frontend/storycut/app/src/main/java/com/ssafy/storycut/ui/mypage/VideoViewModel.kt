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

    fun getNextVideo(currentVideoId: String): VideoDto? {
        val currentList = myVideos.value
        val currentIndex = currentList.indexOfFirst { it.videoId.toString() == currentVideoId }

        // 현재 비디오가 마지막이 아니면 다음 비디오 반환
        return if (currentIndex >= 0 && currentIndex < currentList.size - 1) {
            currentList[currentIndex + 1]
        } else {
            // 마지막 비디오인 경우 처음으로 돌아가거나 null 반환 가능
            null  // 또는 currentList.firstOrNull()
        }
    }


    // 이전 비디오 가져오기
    fun getPreviousVideo(currentVideoId: String): VideoDto? {
        val currentList = myVideos.value
        val currentIndex = currentList.indexOfFirst { it.videoId.toString() == currentVideoId }

        // 현재 비디오가 첫 번째가 아니면 이전 비디오 반환
        return if (currentIndex > 0) {
            currentList[currentIndex - 1]
        } else {
            // 첫 번째 비디오인 경우 마지막으로 이동하거나 null 반환 가능
            null  // 또는 currentList.lastOrNull()
        }
    }
    // 비디오 리스트에서 특정 인덱스부터 원하는 개수만큼 가져오기 (페이징 구현 시 유용)
    fun getVideosFromIndex(startIndex: Int, count: Int = 5): List<VideoDto> {
        val currentList = myVideos.value
        val endIndex = minOf(startIndex + count, currentList.size)

        return if (startIndex < currentList.size) {
            currentList.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }


    private val _roomVideos = MutableStateFlow<List<VideoDto>>(emptyList())
    val roomVideos: StateFlow<List<VideoDto>> = _roomVideos

    // 공유방의 비디오 목록 가져오기
    fun fetchRoomVideos(roomId: String, token: String) {
        viewModelScope.launch {
            try {
                val response = videoRepository.getRoomVideos(roomId, token)
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result?.let {
                        _roomVideos.value = it
                    }
                } else {
                    // 에러 처리
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    Log.d("VideoViewModel", "공유방 비디오 목록 가져오기 실패 : ${errorMsg}")
                }
            } catch (e: Exception) {
                // 예외 처리
                Log.d("VideoViewModel", "공유방 비디오 목록 가져오기 에러 : ${e.message}")
            }
        }
    }
}
