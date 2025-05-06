package com.ssafy.storycut.ui.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.VideoListDto
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

    private val _myVideos = MutableStateFlow<List<VideoListDto>>(emptyList())
    val myVideos: StateFlow<List<VideoListDto>> = _myVideos

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
                    println("비디오 목록 가져오기 실패: $errorMsg")
                }
            } catch (e: Exception) {
                // 예외 처리 - 로그 추가
                println("비디오 목록 가져오기 중 예외 발생: ${e.message}")
            }
        }
    }
}
