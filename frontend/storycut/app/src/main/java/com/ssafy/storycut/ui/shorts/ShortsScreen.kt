// ShortsScreen.kt
package com.ssafy.storycut.ui.shorts

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.livedata.observeAsState
@Composable
fun ShortsScreen(
    viewModel: ShortsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val youtubeAuthResponse by viewModel.youtubeAuthUrl.observeAsState()
    val error by viewModel.error.observeAsState()
    val accessToken by viewModel.accessToken.observeAsState("")
    val scrollState = rememberScrollState()

    var isLoading by remember { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    // 갤러리에서 영상 선택
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }

    LaunchedEffect(Unit) {
        viewModel.loadAccessToken()
    }

    LaunchedEffect(error) {
        error?.let { isLoading = false }
    }

    LaunchedEffect(youtubeAuthResponse) {
        youtubeAuthResponse?.let { response ->
            isLoading = false
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.authUrl))
            context.startActivity(intent)
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "쇼츠 업로드 화면",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (accessToken.isNotEmpty()) {
                Text("✔ 유튜브 액세스 토큰 불러옴", fontWeight = FontWeight.Bold)
            } else {
                Text("❌ 액세스 토큰 없음", color = MaterialTheme.colorScheme.error)
            }

            // 유튜브 권한 요청 버튼 추가
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    isLoading = true
                    viewModel.getYouTubeAuthUrl()
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("🔐 유튜브 권한 요청하기")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { videoPickerLauncher.launch("video/*") },
                enabled = !isLoading
            ) {
                Text("🎞 영상 선택하기")
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedVideoUri?.let { uri ->
                Text("선택된 URI: $uri", maxLines = 1)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        isLoading = true
                        viewModel.uploadToYouTube(
                            videoUri = uri,
                            title = "스토리컷에서 올린 영상",
                            description = "앱에서 자동 업로드된 영상입니다"
                        )
                    },
                    enabled = !isLoading
                ) {
                    Text("🚀 유튜브에 업로드")
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }

        error?.let {
            Snackbar(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(it)
            }
        }
    }
}