import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.storycut.ui.shorts.ShortsUiState
import com.ssafy.storycut.ui.shorts.ShortsViewModel
import com.ssafy.storycut.ui.shorts.components.AuthScreen
import com.ssafy.storycut.ui.shorts.components.UploadScreen
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun ShortsScreen(
    viewModel: ShortsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // UI 상태 관찰
    val uiState by viewModel.uiState.observeAsState(ShortsUiState.Loading)
    val youtubeAuthResponse by viewModel.youtubeAuthUrl.observeAsState()
    val error by viewModel.error.observeAsState()

    var isActionLoading by remember { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리에서 영상 선택
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }

    // 에러 발생 시 로딩 상태 해제
    LaunchedEffect(error) {
        error?.let {
            isActionLoading = false
            // 몇 초 후 에러 메시지 초기화
            delay(3000)
            viewModel.clearError()
        }
    }

    // 인증 URL 응답 처리
    LaunchedEffect(youtubeAuthResponse) {
        youtubeAuthResponse?.let { response ->
            isActionLoading = false
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.authUrl))
            context.startActivity(intent)
        }
    }

    // 메인 화면 구성
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // UI 상태에 따른 화면 표시
        when (uiState) {
            ShortsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            ShortsUiState.Unauthenticated -> {
                AuthScreen(
                    onRequestAuth = {
                        isActionLoading = true
                        viewModel.getYouTubeAuthUrl()
                    },
                    isLoading = isActionLoading
                )
            }

            ShortsUiState.Authenticated -> {
                UploadScreen(
                    selectedVideoUri = selectedVideoUri,
                    onSelectVideo = { videoPickerLauncher.launch("video/*") },
                    onUpload = { uri, title, description, tags ->
                        isActionLoading = true
                        viewModel.uploadToYouTube(
                            videoUri = uri,
                            title = title.ifBlank { "스토리컷에서 올린 영상" },
                            description = description.ifBlank { "앱에서 자동 업로드된 영상입니다" },
                            tags = tags
                        )
                    },
                    isLoading = isActionLoading
                )
            }
        }

        // 에러 메시지 표시 (모든 상태에서 공통)
        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            error?.let {
                Snackbar {
                    Text(it)
                }
            }
        }

        // 액션 진행 중일 때 로딩 인디케이터 (인증/업로드 등)
        if (isActionLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}