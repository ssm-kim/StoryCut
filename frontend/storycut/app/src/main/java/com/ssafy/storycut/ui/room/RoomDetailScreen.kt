package com.ssafy.storycut.ui.room

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ssafy.storycut.R
import com.ssafy.storycut.data.api.model.VideoDto
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.home.HomeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    navController: NavController,
    roomViewModel: RoomViewModel = hiltViewModel(),
    homeViewModel : HomeViewModel = hiltViewModel(),
    tokenManager: TokenManager
) {
    val roomDetail by roomViewModel.roomDetail.collectAsState()
    val roomMembers by roomViewModel.roomMembers.collectAsState()
    val inviteCode by roomViewModel.inviteCode.collectAsState()
//    val videoList by videoViewModel.roomVideos.collectAsState(initial = emptyList())
    val isLoading by roomViewModel.isLoading.collectAsState()
    val error by roomViewModel.error.collectAsState()

    var showInviteCodeDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 토스트 메시지를 위한 함수
    val showToast: (String) -> Unit = { message ->
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // 로딩 시 데이터 가져오기
    LaunchedEffect(roomId) {
        try {
            val token = tokenManager.accessToken.first()
            if (token == null) {
                showToast("로그인이 필요합니다.")
                return@LaunchedEffect
            }
            roomViewModel.getRoomDetail(roomId)
            roomViewModel.getRoomMembers(roomId)
//            videoViewModel.fetchRoomVideos(roomId, token)
        } catch (e: Exception) {
            println("데이터 로드 실패: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("공유방") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로 가기"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error.isNotEmpty()) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 방 정보 헤더
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // 방 제목
                            Text(
                                text = roomDetail?.roomTitle ?: "방 제목",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 방 설명
                            Text(
//                                text = roomDetail?.roomContext ?: "방 설명",
                                text = "방 설명",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // 멤버 정보와 버튼 행
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 멤버 수 표시
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,                                        contentDescription = "멤버",
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Text(
                                        text = "${roomMembers.size}명",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }

                                // 버튼 영역
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 쇼츠 업로드 버튼
                                    Button(
                                        onClick = {
                                            navController.navigate("upload_video/$roomId")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "쇼츠 업로드",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("쇼츠 업로드")
                                    }

                                    // 초대코드 생성 버튼
                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                try {
                                                    roomViewModel.createInviteCode(roomId)
                                                    showInviteCodeDialog = true
                                                } catch (e: Exception) {
                                                    showToast("초대코드 생성 실패")
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(
                                            width = 1.dp
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "초대코드",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("초대코드")
                                    }
                                }
                            }
                        }
                    }

                    // 비디오 목록 제목
                    Text(
                        text = "쇼츠 목록",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
//
//                    // 비디오 그리드
//                    if (videoList.isEmpty()) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp)
//                                .padding(16.dp),
//                            contentAlignment = Alignment.Center
//                        ) {
//                            Column(
//                                horizontalAlignment = Alignment.CenterHorizontally
//                            ) {
//                                Icon(
//                                    imageVector = Icons.Default.Add,
//                                    contentDescription = "비디오 추가",
//                                    modifier = Modifier
//                                        .size(48.dp)
//                                        .padding(8.dp),
//                                    tint = Color.Gray
//                                )
//
//                                Text(
//                                    text = "업로드된 쇼츠가 없습니다",
//                                    color = Color.Gray,
//                                    textAlign = TextAlign.Center
//                                )
//
//                                Spacer(modifier = Modifier.height(16.dp))
//
//                                Button(
//                                    onClick = { navController.navigate("upload_video/$roomId") },
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = MaterialTheme.colorScheme.primary
//                                    )
//                                ) {
//                                    Text("첫 쇼츠 업로드하기")
//                                }
//                            }
//                        }
//                    } else {
//                        LazyVerticalGrid(
//                            columns = GridCells.Fixed(2),
//                            modifier = Modifier.fillMaxSize(),
//                            contentPadding = PaddingValues(8.dp),
//                            verticalArrangement = Arrangement.spacedBy(12.dp),
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            items(videoList.size) { index ->
//                                VideoItem(
//                                    video = videoList[index],
//                                    onClick = {
//                                        navController.navigate("video_detail/${videoList[index].videoId}")
//                                    }
//                                )
//                            }
//                        }
//                    }
                }
            }
        }
    }

    // 초대코드 다이얼로그
    if (showInviteCodeDialog && inviteCode.isNotEmpty()) {
        InviteCodeDialog(
            inviteCode = inviteCode,
            onDismiss = { showInviteCodeDialog = false },
            onCopy = {
                clipboardManager.setText(AnnotatedString(inviteCode))
                showToast("초대코드가 클립보드에 복사되었습니다")
                showInviteCodeDialog = false
            }
        )
    }
}

@Composable
fun VideoItem(
    video: VideoDto,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f/16f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        // 썸네일 이미지
        AsyncImage(
            model = video.thumbnail,
            contentDescription = video.videoName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        // 비디오 정보를 표시하는 오버레이
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(8.dp)
        ) {
            Text(
                text = video.videoName,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    shadow = Shadow(
                        color = Color.Black,
                        blurRadius = 4f,
                        offset = Offset(1f, 1f)
                    )
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}