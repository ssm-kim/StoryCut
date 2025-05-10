package com.ssafy.storycut.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ssafy.storycut.R
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.auth.AuthViewModel
import com.ssafy.storycut.ui.settings.AnimatedSettingsNavigation
import kotlinx.coroutines.flow.first

@Composable
fun MyPageScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    myVideoViewModel: VideoViewModel = hiltViewModel(),
    navController: NavController? = null,
    tokenManager: TokenManager,
    onNavigateToLogin: () -> Unit = {} // 올바른 람다 함수 기본값
) {
    val userInfo by authViewModel.userState.collectAsState()
    val videoList by myVideoViewModel.myVideos.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showSettings by remember { mutableStateOf(false) }
    val createdAt = userInfo?.createdAt
    val postCount = videoList.size
    
    // 날짜 포맷변환
    fun formatCreatedAt(createdAt: String?): String {
        if (createdAt.isNullOrEmpty()) return "정보 없음"

        return try {
            val isoPattern = "yyyy-MM-dd'T'HH:mm:ss"
            val outputPattern = "yyyy.MM.dd"

            val inputFormat = java.text.SimpleDateFormat(isoPattern, java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat(outputPattern, java.util.Locale.getDefault())

            val date = inputFormat.parse(createdAt)
            outputFormat.format(date)
        } catch (e: Exception) {
            createdAt
        }
    }

    LaunchedEffect(Unit) {
        // 사용자 정보 새로고침
        authViewModel.refreshUserInfoFromRoom()

        // 토큰을 가져와서 내 비디오 목록 로드
        try {
            val token = tokenManager.accessToken.first()
            if (!token.isNullOrEmpty()) {
                myVideoViewModel.fetchMyVideos(token)
            }
        } catch (e: Exception) {
            println("토큰 가져오기 실패: ${e.message}")
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 기본 마이페이지 콘텐츠
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (showSettings) 0f else 1f)
        ) {
            // 설정 아이콘
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.setting_icon),
                    contentDescription = "설정",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            showSettings = true
                        }
                )
            }

            // 프로필 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = userInfo?.profileImg,
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = userInfo?.name ?: "사용자", fontWeight = FontWeight.Bold)
                    Text(text = userInfo?.email ?: "이메일 없음", style = MaterialTheme.typography.bodySmall)
                    Text(text = "가입일: ${formatCreatedAt(createdAt)}", style = MaterialTheme.typography.bodySmall)
                    Text(text = "게시글 수: $postCount", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 검색창
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "검색") }, // leading 아이콘 추가
                placeholder = { Text("사진 검색하기") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(vertical = 8.dp), // 상하 패딩 추가
                singleLine = true,
                shape = RoundedCornerShape(32.dp) // 모서리 둥글게 설정
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 비디오 목록 표시
            if (isLoading) {
                // 로딩 중 표시
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (videoList.isEmpty()) {
                // 비디오가 없는 경우
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "첫 쇼츠를 업로드해 보세요",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // 비디오 그리드 표시
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    items(videoList.filter {
                        // 검색어가 비어있지 않으면 필터링, 비어있으면 모든 비디오 표시
                        searchQuery.isEmpty() ||
                        it.videoName.contains(searchQuery, ignoreCase = true)
                    }) { video ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(MaterialTheme.shapes.small)
                                .clickable {
                                    // 비디오 상세 페이지로 이동
                                    navController?.navigate("video_detail/${video.videoId}")
                                }
                        ) {
                            AsyncImage(
                                model = video.thumbnail,
                                contentDescription = video.videoName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,  // 이미지 비율 유지하며 채우기
                                error = painterResource(id = R.drawable.ic_launcher_foreground)
                            )
                        }
                    }
                }
            }
        }

        // 설정 화면 오버레이 (z-index가 더 높아 위에 표시됨)
        if (navController != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(if (showSettings) 1f else 0f)
            ) {
                AnimatedSettingsNavigation(
                    authViewModel = authViewModel,
                    isVisible = showSettings,
                    onDismiss = { showSettings = false },
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}