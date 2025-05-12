package com.ssafy.storycut.ui.home

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.room.RoomDto
import com.ssafy.storycut.data.api.model.MemberDto
import com.ssafy.storycut.data.api.model.room.CreateRoomRequest
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.RoomRepository
import com.ssafy.storycut.data.repository.S3Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val tokenManager: TokenManager,
    private val s3Repository: S3Repository
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

    // 새로 생성된 방 ID
    private val _createdRoomId = MutableLiveData<String>()
    val createdRoomId: LiveData<String> = _createdRoomId

    // 새로 입장한 방 ID
    private val _enteredRoomId = MutableLiveData<String>()
    val enteredRoomId: LiveData<String> = _enteredRoomId

    // 이미지 업로드 상태 추적
    private val _uploadingImage = MutableLiveData<Boolean>()
    val uploadingImage: LiveData<Boolean> = _uploadingImage
    // 내 공유방 목록 조회
    fun getMyRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("RoomDebug", "공유방 목록 로드 시작")

                val token = tokenManager.accessToken.first()
                if (token == null) {
                    Log.e("RoomDebug", "토큰이 없음")
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

                Log.d("RoomDebug", "토큰 확인: ${token.take(10)}...")

                val response = roomRepository.getMyRooms(token)

                Log.d("RoomDebug", "API 응답 코드: ${response.code()}")
                Log.d("RoomDebug", "API 응답 성공 여부: ${response.isSuccessful}")

                if (!response.isSuccessful) {
                    Log.e("RoomDebug", "응답 실패: ${response.errorBody()?.string()}")
                }

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val rooms = response.body()?.result ?: emptyList()
                    Log.d("RoomDebug", "받은 방 목록 수: ${rooms.size}")
                    _myRooms.value = rooms
                } else {
                    val errorMsg = response.body()?.message ?: "공유방 목록을 불러오는데 실패했습니다."
                    Log.e("RoomDebug", "API 에러 메시지: $errorMsg")
                    _error.value = errorMsg
                }
            } catch (e: Exception) {
                Log.e("RoomDebug", "예외 발생", e)
                e.printStackTrace()
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
                Log.d("RoomDebug", "공유방 목록 로드 완료")
            }
        }
    }


    private suspend fun uploadImage(uri: Uri): String {
        _uploadingImage.value = true
        Log.d("ImageUpload", "Starting image upload for URI: $uri")

        return try {
            val token = tokenManager.accessToken.first()
            if (token == null) {
                _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                return "default_thumbnail"
            }

            Log.d("ImageUpload", "Token retrieved, calling S3Repository")
            val response = s3Repository.uploadRoomThumbNailImage(uri)
            Log.d("ImageUpload", "S3 Response: ${response.isSuccessful}, Code: ${response.code()}")

            if (response.isSuccessful && response.body()?.isSuccess == true) {
                try {
                    val result = response.body()?.result
                    Log.d("ImageUpload", "Result: $result")

                    val imageUrls = result?.imageUrls
                    Log.d("ImageUpload", "Image URLs: $imageUrls")

                    if (imageUrls != null && imageUrls.isNotEmpty()) {
                        val relativePath = imageUrls[0]
                        Log.d("ImageUpload", "Selected image URL: $relativePath")
                        // 상대 경로를 전체 URL로 변환
                        val baseImageUrl = "https://storycut-bucket.s3.ap-northeast-2.amazonaws.com/thumbnails/" // 실제 S3 버킷 URL로 변경 필요
                        "$baseImageUrl$relativePath"
                    } else {
                        Log.e("ImageUpload", "No image URLs returned")
                        "default_thumbnail"
                    }
                } catch (e: Exception) {
                    Log.e("ImageUpload", "URL parsing error: ${e.message}", e)
                    _error.value = "이미지 URL 파싱에 실패했습니다."
                    "default_thumbnail"
                }
            } else {
                Log.e("ImageUpload", "Failed response: ${response.body()?.message}")
                _error.value = response.body()?.message ?: "이미지 업로드에 실패했습니다."
                "default_thumbnail"
            }
        } catch (e: Exception) {
            Log.e("ImageUpload", "Exception: ${e.message}", e)
            _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            "default_thumbnail"
        } finally {
            _uploadingImage.value = false
        }
    }

    fun createRoom(request: CreateRoomRequest, imageUri: Uri? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 요청 데이터 로깅
                Log.d("RoomCreate", "Creating room with request: $request")

                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

                // 토큰 로깅 (보안에 주의)
                Log.d("RoomCreate", "Token: ${token.take(10)}...")

                // 이미지가 있으면 먼저 업로드
                val finalRequest = if (imageUri != null) {
                    // 이미지 업로드하고 URL 받기
                    val imageUrl = uploadImage(imageUri)
                    Log.d("RoomCreate", "Image uploaded, URL: $imageUrl")
                    request.copy(roomThumbnail = imageUrl)
                } else {
                    // 기본 이미지 사용
                    Log.d("RoomCreate", "Using default thumbnail")
                    request.copy(roomThumbnail = "default_thumbnail")
                }

                Log.d("RoomCreate", "Final request: $finalRequest")

                val response = roomRepository.createRoom(finalRequest, token)

                // 응답 로깅
                Log.d("RoomCreate", "Response: ${response.code()}, Success: ${response.isSuccessful}")
                Log.d("RoomCreate", "Body: ${response.body()}")

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    response.body()?.result?.let {
                        Log.d("RoomCreate", "Room created successfully with ID: ${it.roomId}")
                        // 새 공유방이 생성되면 목록 다시 불러오기
                        getMyRooms()
                        // 생성된 방의 ID 설정
                        _createdRoomId.value = it.roomId.toString()
                    }
                } else {
                    // 오류 메시지 자세히 로깅
                    Log.e("RoomCreate", "Failed to create room: ${response.body()?.message}")
                    Log.e("RoomCreate", "Error body: ${response.errorBody()?.string()}")
                    _error.value = response.body()?.message ?: "공유방 생성에 실패했습니다."
                }
            } catch (e: Exception) {
                // 예외 자세히 로깅
                Log.e("RoomCreate", "Exception during room creation", e)
                _error.value = e.message ?: "네트워크 오류가 발생했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 생성된 방 ID 초기화
    fun clearCreatedRoomId() {
        _createdRoomId.value = ""
    }

    // 입장한 방 ID 초기화
    fun clearEnteredRoomId() {
        _enteredRoomId.value = ""
    }

    // 공유방 상세 정보 조회
    fun getRoomDetail(roomId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

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
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

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
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

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
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

                val response = roomRepository.enterRoom(inviteCode, token)

                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    // 공유방 입장 후 목록 다시 불러오기
                    getMyRooms()
                    // 입장한 방의 ID 설정
                    response.body()?.result?.let {
                        _enteredRoomId.value = it.roomId.toString()
                    }
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
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

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
                val token = tokenManager.accessToken.first()
                if (token == null) {
                    _error.value = "인증 토큰이 없습니다. 다시 로그인해주세요."
                    return@launch
                }

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