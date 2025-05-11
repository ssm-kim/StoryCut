package com.ssafy.storycut.ui.mypage


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.ssafy.storycut.data.api.model.VideoDto
import com.ssafy.storycut.data.local.datastore.TokenManager
import kotlinx.coroutines.flow.first

@UnstableApi
@Composable
fun VideoDetailScreen(
    videoId: String,  // 비디오 ID (실제 타입에 맞게 조정 필요)
    navController: NavController,
    videoViewModel: VideoViewModel = hiltViewModel(),
    tokenManager: TokenManager
) {
    val context = LocalContext.current
    var video by remember { mutableStateOf<VideoDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isPlaying by remember { mutableStateOf(true) }  // 재생 상태 추적

    // ExoPlayer 인스턴스 생성
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true  // 초기에는 자동 재생
            repeatMode = Player.REPEAT_MODE_ONE  // 비디오 반복 재생
        }
    }

    // 비디오 로드
    LaunchedEffect(videoId) {
        isLoading = true
        try {
            // 먼저 캐시에서 비디오 확인
            val cachedVideo = videoViewModel.getVideoFromCache(videoId)
            if (cachedVideo != null) {
                video = cachedVideo
                setupExoPlayer(cachedVideo, exoPlayer)
            }

            // 토큰 가져오기
            val token = tokenManager.accessToken.first()
            if (!token.isNullOrEmpty()) {
                // API에서 최신 정보 가져오기
                val videoDetail = videoViewModel.getVideoDetail(videoId, token)
                if (videoDetail != null) {
                    video = videoDetail
                    setupExoPlayer(videoDetail, exoPlayer)
                } else if (video == null) {
                    error = "비디오를 찾을 수 없습니다."
                }
            } else {
                if (video == null) {
                    error = "인증 정보가 없습니다. 다시 로그인해주세요."
                }
            }
        } catch (e: Exception) {
            error = "비디오를 로드할 수 없습니다: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // 화면이 종료되면 ExoPlayer 해제
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            // 전체 화면을 클릭하면 재생/정지 토글
            .clickable {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                } else {
                    exoPlayer.play()
                }
                isPlaying = !isPlaying
            }
    ) {
        when {
            isLoading -> {
                // 로딩 표시
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            error != null -> {
                // 에러 표시
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error ?: "오류가 발생했습니다",
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("돌아가기")
                    }
                }
            }
            else -> {
                // 비디오 플레이어 (전체 화면, 컨트롤러 숨김)
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false  // 기본 컨트롤러 숨기기
                            setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 재생/정지 아이콘 (정지 상태일 때만 표시)
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "재생",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // 간략한 비디오 정보 (하단에 표시)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 비디오 제목
                        Text(
                            text = video?.videoName ?: "",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 업로더 정보 (VideoDto의 실제 필드에 맞게 수정 필요)
                        Text(
                            text = video?.videoName ?: "알 수 없음",  // 필드명 확인 필요
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

// ExoPlayer 설정 함수
@UnstableApi
private fun setupExoPlayer(video: VideoDto, exoPlayer: ExoPlayer) {
    // videoUrl 필드 이름은 실제 VideoDto 클래스에 맞게 수정
    video.videoUrl.let { url ->  // 필드명 확인 필요
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }
}