package com.ssafy.storycut.ui.room

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.MemberDto
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

    // 초대코드 생성
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

    // 에러 메시지 초기화
    fun clearError() {
        _error.value = ""
    }


}