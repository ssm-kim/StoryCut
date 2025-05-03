package com.ssafy.storycut.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.repository.AuthRepository
import com.ssafy.storycut.data.repository.GoogleAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val googleAuthService: GoogleAuthService
) : ViewModel() {

    // UI 상태
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // 토큰 상태
    private val _tokenState = MutableStateFlow<Pair<String, String>?>(null)
    val tokenState: StateFlow<Pair<String, String>?> = _tokenState.asStateFlow()

    // 구글 로그인 시작
    fun signInWithGoogle(context: Context, credentialManager: CredentialManager) {
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            googleAuthService.signInWithGoogle(context, credentialManager) { idToken ->
                // 서버에 토큰 전송
                sendTokenToServer(idToken)
            }
        }
    }

    // 서버에 토큰 전송
    private fun sendTokenToServer(idToken: String) {
        viewModelScope.launch {
            authRepository.sendTokenToServer(idToken) { result, tokens ->
                if (tokens != null) {
                    _tokenState.value = tokens
                    _uiState.value = AuthUiState.Success(result)
                } else {
                    _uiState.value = AuthUiState.Error(result)
                }
            }
        }
    }
}

// UI 상태를 표현하는 sealed 클래스
sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val message: String) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}