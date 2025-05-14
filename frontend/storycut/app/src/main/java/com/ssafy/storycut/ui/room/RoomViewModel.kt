package com.ssafy.storycut.ui.room

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.MemberDto
import com.ssafy.storycut.data.api.model.chat.ChatDto
import com.ssafy.storycut.data.api.model.chat.ChatMessageRequest
import com.ssafy.storycut.data.api.model.room.RoomDto
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _roomDetail = MutableStateFlow<RoomDto?>(null)
    val roomDetail: StateFlow<RoomDto?> = _roomDetail

    private val _roomMembers = MutableStateFlow<List<MemberDto>>(emptyList())
    val roomMembers: StateFlow<List<MemberDto>> = _roomMembers

    private val _inviteCode = MutableStateFlow("")
    val inviteCode: StateFlow<String> = _inviteCode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error

    private val _inviteCodeCreationTime = MutableStateFlow<Long>(0)
    val inviteCodeCreationTime: StateFlow<Long> = _inviteCodeCreationTime

    // 비디오 업로드 성공 여부
    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess

    // 비디오 목록 상태
    private val _roomVideos = MutableStateFlow<List<ChatDto>>(emptyList())
    val roomVideos: StateFlow<List<ChatDto>> = _roomVideos

    // 페이징 관련 상태
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _hasMoreVideos = MutableStateFlow(true)
    val hasMoreVideos: StateFlow<Boolean> = _hasMoreVideos

    // 비디오 목록 로딩 상태
    private val _isVideosLoading = MutableStateFlow(false)
    val isVideosLoading: StateFlow<Boolean> = _isVideosLoading

    // 공유방 상세 정보 가져오기
    fun getRoomDetail(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                Log.d("RoomViewModel", "Loading room details for roomId: $roomId")
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    Log.e("RoomViewModel", "Token is null")
                    return@launch
                }

                Log.d("RoomViewModel", "Calling API with token: ${token.take(10)}...")
                val response = roomRepository.getRoomDetail(roomId, token)

                Log.d("RoomViewModel", "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    Log.d("RoomViewModel", "Response successful: ${response.body()}")
                } else {
                    Log.e("RoomViewModel", "Error response: ${response.errorBody()?.string()}")
                }

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _roomDetail.value = response.body()?.result
                    Log.d("RoomViewModel", "Room detail loaded: ${_roomDetail.value}")
                } else {
                    _error.value = response.body()?.message ?: "공유방 정보를 불러오는데 실패했습니다."
                    Log.e("RoomViewModel", "Failed to load room detail: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
                Log.e("RoomViewModel", "Exception loading room detail", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 멤버 목록 가져오기
    fun getRoomMembers(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }
                val response = roomRepository.getRoomMembers(roomId, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _roomMembers.value = response.body()?.result ?: emptyList()
                } else {
                    _error.value = response.body()?.message ?: "멤버 목록을 불러오는데 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }
    // createInviteCode 함수 수정
    fun createInviteCode(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }
                val response = roomRepository.createInviteCode(roomId, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _inviteCode.value = response.body()?.result ?: ""
                    _inviteCodeCreationTime.value = System.currentTimeMillis() // 코드 생성 시간 저장
                } else {
                    _error.value = response.body()?.message ?: "초대코드 생성에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun uploadShort(
        roomId: String,
        videoUrl: String,
        title: String,
        thumbnailUrl: String,
        videoId: String = ""
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""
            _uploadSuccess.value = false

            try {
                Log.d("RoomViewModel", "Uploading short for roomId: $roomId with title: $title")
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    Log.e("RoomViewModel", "Token is null")
                    return@launch
                }

                // 업로드할 메시지 요청 객체 생성
                val chatMessage = ChatMessageRequest(
                    videoId = videoId,
                    title = title,
                    mediaUrl = videoUrl,
                    thumbnailUrl = thumbnailUrl
                )

                // 방 ID를 Long으로 변환
                val roomIdLong = try {
                    roomId.toLong()
                } catch (e: Exception) {
                    _error.value = "잘못된 방 ID 형식입니다."
                    Log.e("RoomViewModel", "Invalid room ID format", e)
                    return@launch
                }

                // 리포지토리를 통해 API 호출
                val response = roomRepository.uploadRoomVideo(roomIdLong, chatMessage, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    Log.d("RoomViewModel", "Short upload successful: ${response.body()}")
                    _uploadSuccess.value = true
                } else {
                    _error.value = response.body()?.message ?: "쇼츠 업로드에 실패했습니다."
                    Log.e("RoomViewModel", "Failed to upload short: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
                Log.e("RoomViewModel", "Exception uploading short", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 비디오 목록 조회
    fun getRoomVideos(roomId: String, refresh: Boolean = false) {
        viewModelScope.launch {
            _isVideosLoading.value = true
            _error.value = ""

            // 새로고침인 경우 페이지를 0으로 초기화
            if (refresh) {
                _currentPage.value = 0
                _roomVideos.value = emptyList()
                _hasMoreVideos.value = true
            }

            // 더 이상 불러올 데이터가 없는 경우 중단
            if (!_hasMoreVideos.value && !refresh) {
                _isVideosLoading.value = false
                return@launch
            }

            try {
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    Log.e("RoomViewModel", "Token is null")
                    return@launch
                }

                // 방 ID를 Long으로 변환
                val roomIdLong = try {
                    roomId.toLong()
                } catch (e: Exception) {
                    _error.value = "잘못된 방 ID 형식입니다."
                    Log.e("RoomViewModel", "Invalid room ID format", e)
                    return@launch
                }

                Log.d("RoomViewModel", "Loading videos for roomId: $roomId, page: ${_currentPage.value}")
                val response = roomRepository.getRoomVideos(
                    roomId = roomIdLong,
                    page = _currentPage.value,
                    size = 10,
                    token = token
                )

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val videos = response.body()?.result ?: emptyList()

                    // 데이터가 더 있는지 여부 확인 (10개 미만이면 더 이상 데이터가 없음)
                    _hasMoreVideos.value = videos.size == 10

                    // 기존 목록에 새로 불러온 항목 추가 (페이지 > 0인 경우)
                    if (_currentPage.value > 0 && !refresh) {
                        _roomVideos.value = _roomVideos.value + videos
                    } else {
                        // 첫 페이지이거나 새로고침인 경우 새 목록으로 대체
                        _roomVideos.value = videos
                    }

                    // 성공적으로 로드했으면 다음 페이지 준비
                    if (videos.isNotEmpty()) {
                        _currentPage.value = _currentPage.value + 1
                    }

                    Log.d("RoomViewModel", "Loaded ${videos.size} videos, total: ${_roomVideos.value.size}")
                } else {
                    _error.value = response.body()?.message ?: "비디오 목록을 불러오는데 실패했습니다."
                    Log.e("RoomViewModel", "Failed to load videos: ${_error.value}")
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
                Log.e("RoomViewModel", "Exception loading videos", e)
            } finally {
                _isVideosLoading.value = false
            }
        }
    }

    // 비디오 목록 새로고침
    fun refreshRoomVideos(roomId: String) {
        getRoomVideos(roomId, true)
    }

    // 더 많은 비디오 불러오기
    fun loadMoreVideos(roomId: String) {
        if (!_isVideosLoading.value && _hasMoreVideos.value) {
            getRoomVideos(roomId)
        }
    }


    // 에러 메시지 초기화
    fun clearError() {
        _error.value = ""
    }

    // 업로드 상태 초기화
    fun resetUploadState() {
        _uploadSuccess.value = false
    }
}