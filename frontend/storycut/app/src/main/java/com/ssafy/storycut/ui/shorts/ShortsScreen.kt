import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.ssafy.storycut.ui.shorts.ShortsViewModel
import com.ssafy.storycut.ui.shorts.components.AuthScreen
import com.ssafy.storycut.ui.shorts.components.UploadScreen
import androidx.compose.ui.graphics.Color


@Composable
fun ShortsScreen(
    viewModel: ShortsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val youtubeAuthResponse by viewModel.youtubeAuthUrl.observeAsState()
    val error by viewModel.error.observeAsState()
    val accessToken by viewModel.accessToken.observeAsState("")

    var isLoading by remember { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리에서 영상 선택
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }

    // 초기 액세스 토큰 로드
    LaunchedEffect(Unit) {
        viewModel.loadAccessToken()
    }

    // 에러 발생 시 로딩 상태 해제
    LaunchedEffect(error) {
        error?.let { isLoading = false }
    }

    // 인증 URL 응답 처리
    LaunchedEffect(youtubeAuthResponse) {
        youtubeAuthResponse?.let { response ->
            isLoading = false
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
        // 구글 인증 토큰 없으면 받아오기
        if (accessToken.isEmpty()) {
            AuthScreen(
                onRequestAuth = {
                    isLoading = true
                    viewModel.getYouTubeAuthUrl()
                },
                isLoading = isLoading
            )
        } else {
            // 인증된 경우 - 업로드 화면 표시
            UploadScreen(
                selectedVideoUri = selectedVideoUri,
                onSelectVideo = { videoPickerLauncher.launch("video/*") },
                onUpload = { uri, title, description, tags ->  // 태그 매개변수 추가
                    isLoading = true
                    viewModel.uploadToYouTube(
                        videoUri = uri,
                        title = title.ifBlank { "스토리컷에서 올린 영상" },
                        description = description.ifBlank { "앱에서 자동 업로드된 영상입니다" },
                        tags = tags
                    )
                },
                isLoading = isLoading
            )
        }

        // 에러 메시지 표시
        error?.let {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(it)
            }
        }

        // 로딩 인디케이터
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}