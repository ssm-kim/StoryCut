package com.ssafy.storycut.ui.mypage

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.first

@UnstableApi
@Composable
fun VideoDetailScreen(
    videoId: String,
    navController: NavController,
    videoViewModel: VideoViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),  // AuthViewModel 추가
    tokenManager: TokenManager
) {
    val context = LocalContext.current
    val videoList by videoViewModel.myVideos.collectAsState()
    val userInfo by authViewModel.userState.collectAsState()  // 사용자 정보 가져오기
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 화면 종료 여부를 추적하는 변수
    var isExiting by remember { mutableStateOf(false) }

    // 플레이어 맵을 관리하기 위한 변수
    val players = remember { mutableMapOf<Int, ExoPlayer>() }

    // 앱 생명주기 상태를 저장
    var appInBackground by remember { mutableStateOf(false) }

    // 현재 선택된 비디오의 위치 찾기
    val initialPage = remember(videoId, videoList) {
        videoList.indexOfFirst { it.videoId.toString() == videoId }.takeIf { it >= 0 } ?: 0
    }

    // VerticalPager 상태 설정
    val pagerState = rememberPagerState(initialPage = initialPage) { videoList.size }

    // 뒤로가기 처리 함수 정의 - 먼저 선언
    val handleBackPress = {
        // 종료 상태로 변경하고 모든 플레이어 즉시 해제
        isExiting = true
        players.values.forEach { player ->
            player.stop()
            player.release()
        }
        players.clear()

        // 화면 전환
        navController.popBackStack()
    }

    // 생명주기 감지를 위한 효과
    DisposableEffect(true) {
        val activity = context as? androidx.activity.ComponentActivity
        val callback = object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPress()
            }
        }

        val lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                // 백그라운드로 이동할 때
                appInBackground = true
                players.values.forEach { player ->
                    if (player.isPlaying) {
                        player.pause()
                    }
                }
            }

            override fun onResume(owner: LifecycleOwner) {
                // 포그라운드로 돌아올 때
                if (appInBackground) {
                    appInBackground = false
                    val currentPage = pagerState.currentPage
                    players[currentPage]?.play()
                }
            }
        }

        activity?.lifecycle?.addObserver(lifecycleObserver)
        activity?.onBackPressedDispatcher?.addCallback(callback)

        onDispose {
            activity?.lifecycle?.removeObserver(lifecycleObserver)
            callback.remove()
        }
    }

    // 안드로이드 시스템 뒤로가기 버튼 처리
    BackHandler(enabled = !isExiting) {
        handleBackPress()
    }

    // 비디오 로드 및 토큰 가져오기
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val token = tokenManager.accessToken.first()
            if (!token.isNullOrEmpty()) {
                // 사용자 정보 새로고침
                authViewModel.refreshUserInfoFromRoom()
                
                // 만약 비디오 리스트가 비어있다면 불러오기
                if (videoList.isEmpty()) {
                    videoViewModel.fetchMyVideos(token)
                }
                // 현재 비디오 상세정보 불러오기
                videoViewModel.getVideoDetail(videoId, token)
            } else {
                error = "인증 정보가 없습니다. 다시 로그인해주세요."
            }
        } catch (e: Exception) {
            error = "비디오를 로드할 수 없습니다: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // 화면 종료 시 모든 플레이어 해제
    DisposableEffect(Unit) {
        onDispose {
            players.values.forEach { player ->
                player.release()
            }
            players.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            error != null -> {
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
                    Button(onClick = { handleBackPress() }) {
                        Text("돌아가기")
                    }
                }
            }
            videoList.isEmpty() -> {
                Text(
                    text = "비디오가 없습니다",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            else -> {
                if (!isExiting) {
                    // VerticalPager로 정확히 한 비디오씩만 스와이프 가능하게 구현
                    VerticalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .systemBarsPadding() // 시스템 바 영역 제외
                    ) { page ->
                        val video = videoList.getOrNull(page)
                        if (video != null) {
                            SingleVideoPlayer(
                                video = video,
                                isCurrentlyVisible = page == pagerState.currentPage && !isExiting && !appInBackground,
                                onPlayerCreated = { player ->
                                    players[page] = player
                                },
                                // 사용자 정보 전달
                                userProfileImg = userInfo?.profileImg,
                                userName = userInfo?.name,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // 뒤로가기 버튼
                IconButton(
                    onClick = { handleBackPress() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }
            }
        }
    }
}