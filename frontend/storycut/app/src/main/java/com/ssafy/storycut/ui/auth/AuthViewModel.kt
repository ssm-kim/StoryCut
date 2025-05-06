package com.ssafy.storycut.ui.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.UserInfo
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.AuthRepository
import com.ssafy.storycut.data.repository.GoogleAuthService
import com.ssafy.storycut.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleAuthService: GoogleAuthService,
    private val userRepository: UserRepository, // Room DB 리포지토리 추가
    private val tokenManager: TokenManager // TokenManager 추가
) : ViewModel() {

    // UI 상태
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 토큰 상태
    private val _tokenState = MutableStateFlow<Pair<String, String>?>(null)
    val tokenState: StateFlow<Pair<String, String>?> = _tokenState.asStateFlow()
    
    // 사용자 정보 상태
    private val _userState = MutableStateFlow<UserInfo?>(null)
    val userState: StateFlow<UserInfo?> = _userState.asStateFlow()

    // 초기화 시 Room DB에서 사용자 정보 불러오기
    init {
        // 토큰 확인 후 자동 로그인 시도
        checkTokenAndAutoLogin()
    }
    
    // 토큰 확인 및 자동 로그인 시도
    private fun checkTokenAndAutoLogin() {
        viewModelScope.launch {
            try {
                // 액세스 토큰 확인
                val accessToken = tokenManager.accessToken.first()
                
                if (!accessToken.isNullOrEmpty()) {
                    Log.d(TAG, "액세스 토큰 발견, 자동 로그인 시도")
                    
                    // 사용자 정보 가져오기 시도
                    tryLoadUserInfo()
                } else {
                    Log.d(TAG, "액세스 토큰 없음, 리프레시 토큰 확인")
                    
                    // 리프레시 토큰 확인
                    val refreshToken = tokenManager.refreshToken.first()
                    
                    if (!refreshToken.isNullOrEmpty()) {
                        Log.d(TAG, "리프레시 토큰 발견, 토큰 갱신 시도")
                        
                        // 토큰 갱신 시도
                        val newTokens = authRepository.refreshAccessToken()
                        
                        if (newTokens != null) {
                            Log.d(TAG, "토큰 갱신 성공, 사용자 정보 가져오기 시도")
                            
                            // 사용자 정보 가져오기 시도
                            tryLoadUserInfo()
                        } else {
                            Log.e(TAG, "토큰 갱신 실패, Room DB에서 사용자 정보 조회")
                            
                            // Room DB에서 사용자 정보 조회
                            loadUserFromDatabase()
                        }
                    } else {
                        Log.d(TAG, "리프레시 토큰도 없음, Room DB에서 사용자 정보 조회")
                        
                        // Room DB에서 사용자 정보 조회
                        loadUserFromDatabase()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 확인 중 오류", e)
                
                // 오류 발생 시 Room DB에서 사용자 정보 조회
                loadUserFromDatabase()
            }
        }
    }
    
    // 사용자 정보 가져오기 시도
    private fun tryLoadUserInfo() {
        viewModelScope.launch {
            try {
                val userInfo = authRepository.getUserInfo()
                
                if (userInfo != null) {
                    // 사용자 정보 업데이트
                    _userState.value = userInfo
                    _uiState.value = AuthUiState.Success("자동 로그인 성공")
                    Log.d(TAG, "서버에서 사용자 정보 가져오기 성공: $userInfo")
                    
                    // Room DB에 사용자 정보 저장
                    saveUserToDatabase(userInfo)
                } else {
                    Log.e(TAG, "서버에서 사용자 정보를 가져오는 데 실패")
                    
                    // 서버에서 사용자 정보를 가져오지 못한 경우 Room DB에서 조회
                    loadUserFromDatabase()
                }
            } catch (e: Exception) {
                Log.e(TAG, "서버에서 사용자 정보 가져오기 중 오류", e)
                
                // 오류 발생 시 Room DB에서 사용자 정보 조회
                loadUserFromDatabase()
            }
        }
    }
    
    // 마이페이지에서 사용할 Room DB 최신 사용자 정보 갱신 메서드
    fun refreshUserInfoFromRoom() {
        loadUserFromDatabase()
    }
    
    // Room DB에서 사용자 정보 불러오기
    private fun loadUserFromDatabase() {
        viewModelScope.launch {
            try {
                // Room DB에서 가장 최신 사용자 정보를 가져오기 위해 first()를 사용
                // Flow에서 최신값을 반드시 가져온다
                val user = userRepository.getCurrentUser().first()
                if (user != null) {
                    _userState.value = user
                    // init과 새로고침 메서드를 구분하기 위해 상태를 업데이트하지 않음
                    // _uiState.value = AuthUiState.Success("로그인 정보 복원 성공")
                    Log.d(TAG, "Room DB에서 사용자 정보 불러오기 성공: $user")
                } else {
                    Log.d(TAG, "Room DB에 저장된 사용자 정보 없음")
                    // 서버에서 사용자 정보를 다시 가져오기 시도
                    try {
                        getUserInfo()
                    } catch (e: Exception) {
                        Log.e(TAG, "서버에서 사용자 정보 가져오기 실패", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Room DB에서 사용자 정보 불러오기 실패", e)
            }
        }
    }

    // 구글 로그인 시작
    fun signInWithGoogle(context: Context, credentialManager: CredentialManager) {
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            try {
                googleAuthService.signInWithGoogle(context, credentialManager) { idToken ->
                    // 서버에 토큰 전송
                    sendTokenToServer(idToken)
                }
            } catch (e: Exception) {
                Log.e(TAG, "로그인 중 오류 발생", e)
                _uiState.value = AuthUiState.Error(e.message ?: "로그인 중 오류가 발생했습니다.")
            }
        }
    }

    // 서버에 토큰 전송
    private fun sendTokenToServer(idToken: String) {
        viewModelScope.launch {
            try {
                authRepository.sendTokenToServer(idToken) { result, tokens ->
                    if (tokens != null) {
                        _tokenState.value = tokens
                        
                        // 로그인 성공 시 사용자 정보 가져오기
                        getUserInfo()
                    } else {
                        _uiState.value = AuthUiState.Error(result)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "토큰 전송 중 오류 발생", e)
                _uiState.value = AuthUiState.Error(e.message ?: "서버 통신 중 오류가 발생했습니다.")
            }
        }
    }
    
    // 사용자 정보 가져오기
    private fun getUserInfo() {
        viewModelScope.launch {
            try {
                val userInfo = authRepository.getUserInfo()
                if (userInfo != null) {
                    // 사용자 정보 저장 및 성공 상태로 변경
                    _userState.value = userInfo
                    _uiState.value = AuthUiState.Success("로그인 성공")
                    Log.d(TAG, "사용자 정보 가져오기 성공: $userInfo")
                    
                    // Room DB에 사용자 정보 저장
                    saveUserToDatabase(userInfo)
                } else {
                    // 사용자 정보를 가져오는데 실패했지만, 로그인은 성공
                    // 이 경우에도 메인 화면으로 이동할 수 있도록 성공 상태로 변경
                    _uiState.value = AuthUiState.Success("로그인 성공했지만 사용자 정보를 가져오는데 실패했습니다.")
                    Log.e(TAG, "사용자 정보 가져오기 실패")
                }
            } catch (e: Exception) {
                Log.e(TAG, "사용자 정보 가져오기 중 오류 발생", e)
                // 사용자 정보를 가져오는데 실패했지만, 로그인은 성공
                // 이 경우에도 메인 화면으로 이동할 수 있도록 성공 상태로 변경
                _uiState.value = AuthUiState.Success("로그인 성공했지만 사용자 정보를 가져오는데 실패했습니다.")
            }
        }
    }
    
    // Room DB에 사용자 정보 저장
    private fun saveUserToDatabase(userInfo: UserInfo) {
        viewModelScope.launch {
            try {
                userRepository.saveUser(userInfo)
                Log.d(TAG, "Room DB에 사용자 정보 저장 성공")
            } catch (e: Exception) {
                Log.e(TAG, "Room DB에 사용자 정보 저장 실패", e)
            }
        }
    }
    
    // 로그아웃
    fun logout() {
        viewModelScope.launch {
            try {
                // Room DB에서 사용자 정보 삭제
                userRepository.logout()
                
                // 상태 초기화
                _userState.value = null
                _tokenState.value = null
                _uiState.value = AuthUiState.Initial
                
                Log.d(TAG, "로그아웃 성공")
            } catch (e: Exception) {
                Log.e(TAG, "로그아웃 중 오류 발생", e)
            }
        }
    }
    
    // 로그인 성공 상태로 설정
    fun setLoginSuccess() {
        _uiState.value = AuthUiState.Success("로그인 성공")
    }
}

// UI 상태를 표현하는 sealed 클래스
sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}