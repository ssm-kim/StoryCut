package com.ssafy.storycut.ui.shorts

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssafy.storycut.data.api.model.credential.GooglePermissionResponse
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortsViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _youtubeAuthUrl = MutableLiveData<GooglePermissionResponse?>()
    val youtubeAuthUrl: MutableLiveData<GooglePermissionResponse?> = _youtubeAuthUrl

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _accessToken = MutableLiveData<String>()
    val accessToken: LiveData<String> = _accessToken

    fun getYouTubeAuthUrl() {
        viewModelScope.launch {
            try {
                val response = repository.getYouTubeAuthUrl()
                _youtubeAuthUrl.value = response
            } catch (e: Exception) {
                _error.value = "인증 URL을 가져오는 데 실패했습니다: ${e.message}"
            }
        }
    }

    fun loadAccessToken() {
        viewModelScope.launch {
            try {
                val token = tokenManager.googleAccessToken.first()
                _accessToken.value = token ?: ""
            } catch (e: Exception) {
                _error.value = "토큰을 불러오는 데 실패했습니다: ${e.message}"
            }
        }
    }



    fun uploadToYouTube(videoUri: Uri, title: String, description: String) {
        viewModelScope.launch {
            try {
                val token = tokenManager.googleAccessToken.first() ?: ""
                Log.d("ShortsViewModel", "토큰 가져오기 성공: ${token}")

                repository.uploadVideoToYouTube(token, videoUri, title, description)
                _error.value = "유튜브 업로드가 성공적으로 시작되었습니다!"
            } catch (e: Exception) {
                _error.value = "업로드 실패: ${e.message}"
                Log.d("ShortsViewModel","에러 ${e.message}")
            }
        }
    }


}
