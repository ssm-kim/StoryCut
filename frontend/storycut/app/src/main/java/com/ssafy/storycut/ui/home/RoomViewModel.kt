package com.ssafy.storycut.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.room.RoomDto
import com.ssafy.storycut.data.api.model.MemberDto
import com.ssafy.storycut.data.api.model.room.CreateRoomRequest
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // 내 공유방 목록 LiveData
    private val _myRooms = MutableLiveData<List<RoomDto>>()
    val myRooms: LiveData<List<RoomDto>> = _myRooms

    // 단일 공유방 상세 정보 LiveData
    private val _roomDetail = MutableLiveData<RoomDto>()
    val roomDetail: LiveData<RoomDto> = _roomDetail

    // 공유방 참여자 목록 LiveData
    private val _roomMembers = MutableLiveData<List<MemberDto>>()
    val roomMembers: LiveData<List<MemberDto>> = _roomMembers

    // 초대 코드 LiveData
    private val _inviteCode = MutableLiveData<String>()
    val inviteCode: LiveData<String> = _inviteCode

    // 로딩 상태 LiveData
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 오류 메시지 LiveData
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // 내 공유방 목록 조회
    fun getMyRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.getMyRooms(token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _myRooms.value = response.body()?.result ?: emptyList()
                } else {
                    _error.value = response.body()?.message ?: "공유방 목록을 불러오는데 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 생성
    fun createRoom(request: CreateRoomRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.createRoom(request, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result?.let {
                        // 새 공유방이 생성되면 목록 다시 불러오기
                        getMyRooms()
                    }
                } else {
                    _error.value = response.body()?.message ?: "공유방 생성에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 상세 정보 조회
    fun getRoomDetail(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.getRoomDetail(roomId, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result?.let {
                        _roomDetail.value = it
                    }
                } else {
                    _error.value = response.body()?.message ?: "공유방 정보를 불러오는데 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 참여자 목록 조회
    fun getRoomMembers(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.getRoomMembers(roomId, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    _roomMembers.value = response.body()?.result ?: emptyList()
                } else {
                    _error.value = response.body()?.message ?: "참여자 목록을 불러오는데 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 초대 코드 생성
    fun createInviteCode(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.createInviteCode(roomId, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result?.let {
                        _inviteCode.value = it
                    }
                } else {
                    _error.value = response.body()?.message ?: "초대 코드 생성에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 입장
    fun enterRoom(inviteCode: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.enterRoom(inviteCode, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 공유방 입장 후 목록 다시 불러오기
                    getMyRooms()
                } else {
                    _error.value = response.body()?.message ?: "공유방 입장에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 나가기
    fun leaveRoom(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.leaveRoom(roomId, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 공유방 나간 후 목록 다시 불러오기
                    getMyRooms()
                } else {
                    _error.value = response.body()?.message ?: "공유방 나가기에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 공유방 삭제
    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first() ?: ""
                val response = roomRepository.deleteRoom(roomId, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 공유방 삭제 후 목록 다시 불러오기
                    getMyRooms()
                } else {
                    _error.value = response.body()?.message ?: "공유방 삭제에 실패했습니다."
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 오류 메시지 초기화
    fun clearError() {
        _error.value = ""
    }
}