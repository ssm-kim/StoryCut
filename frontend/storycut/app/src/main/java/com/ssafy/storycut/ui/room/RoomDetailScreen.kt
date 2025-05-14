package com.ssafy.storycut.ui.room

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ssafy.storycut.R
import com.ssafy.storycut.data.api.model.VideoDto
import com.ssafy.storycut.data.api.model.chat.ChatDto
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.home.HomeViewModel
import com.ssafy.storycut.ui.mypage.VideoViewModel
import com.ssafy.storycut.ui.room.dialog.UploadShortDialog
import com.ssafy.storycut.ui.room.upload.VideoSelectionItem
import com.ssafy.storycut.ui.room.upload.VideoSelectorFullScreenDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDetailScreen(
    roomId: String,
    navController: NavController,
    roomViewModel: RoomViewModel = hiltViewModel(),
    videoViewModel: VideoViewModel = hiltViewModel(),
    tokenManager: TokenManager
) {
    val roomDetail by roomViewModel.roomDetail.collectAsState()
    val roomMembers by roomViewModel.roomMembers.collectAsState()
    val inviteCode by roomViewModel.inviteCode.collectAsState()
    val isLoading by roomViewModel.isLoading.collectAsState()
    val error by roomViewModel.error.collectAsState()
    val uploadSuccess by roomViewModel.uploadSuccess.collectAsState()
    val myVideos by videoViewModel.myVideos.collectAsState()

    // 비디오 관련 상태 가져오기
    val roomVideos by roomViewModel.roomVideos.collectAsState()
    val isVideosLoading by roomViewModel.isVideosLoading.collectAsState()
    val hasMoreVideos by roomViewModel.hasMoreVideos.collectAsState()
    val currentPage by roomViewModel.currentPage.collectAsState()

    var showInviteCodeDialog by remember { mutableStateOf(false) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showVideoSelectorDialog by remember { mutableStateOf(false) }
    var selectedVideo by remember { mutableStateOf<VideoDto?>(null) }
    var title by remember { mutableStateOf("") }
    var isGeneratingCode by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 다이얼로그 상태 초기화 함수
    fun resetDialogStates() {
        selectedVideo = null
        title = ""
    }

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
            roomViewModel.getRoomVideos(roomId) // 비디오 목록 로드 추가

            // 내 비디오 목록도 미리 로드
            videoViewModel.fetchMyVideos(token)
        } catch (e: Exception) {
            println("데이터 로드 실패: ${e.message}")
        }
    }

    // 업로드 성공 시 처리
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            showToast("쇼츠가 성공적으로 업로드되었습니다")
            roomViewModel.resetUploadState()
            // 업로드 성공 후 비디오 목록 새로고침
            roomViewModel.refreshRoomVideos(roomId)
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
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "멤버",
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
                                            showUploadDialog = true
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

                                    // 초대코드 생성 버튼 (수정된 부분)
                                    OutlinedButton(
                                        onClick = {
                                            if (inviteCode.isNotEmpty()) {
                                                // 이미 코드가 있으면 바로 다이얼로그 표시
                                                showInviteCodeDialog = true
                                            } else {
                                                // 코드가 없는 경우에만 API 호출
                                                scope.launch {
                                                    try {
                                                        isGeneratingCode = true
                                                        roomViewModel.createInviteCode(roomId)
                                                        showInviteCodeDialog = true
                                                    } catch (e: Exception) {
                                                        showToast("초대코드 생성 실패")
                                                    } finally {
                                                        isGeneratingCode = false
                                                    }
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
                                        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                                        enabled = !isLoading && !isGeneratingCode
                                    ) {
                                        Box(
                                            modifier = Modifier.height(24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isGeneratingCode) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.copy),
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

                    // 비디오 목록 또는 비디오가 없을 때 메시지 표시
                    if (roomVideos.isEmpty()) {
                        // 비디오가 없는 경우 메시지 표시
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "비디오 추가",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(8.dp),
                                    tint = Color.Gray
                                )

                                Text(
                                    text = "업로드된 쇼츠가 없습니다",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showUploadDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("첫 쇼츠 업로드하기")
                                }
                            }
                        }
                    } else {
                        // 비디오 목록이 있는 경우 LazyVerticalGrid로 표시
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // 남은 공간 채우기
                        ) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(roomVideos) { video ->
                                    RoomVideoItem(
                                        video = video,
                                        onClick = {
                                            // 비디오 클릭 시 처리 (예: 상세 화면으로 이동 또는 재생)
                                            // navController.navigate("video_player/${video.id}")
                                        }
                                    )
                                }

                                // 더 불러오기 아이템
                                item(span = { GridItemSpan(2) }) {
                                    if (hasMoreVideos) {
                                        // 더 불러오기 버튼 또는 로딩 표시
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isVideosLoading && currentPage > 0) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(32.dp),
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Button(
                                                    onClick = { roomViewModel.loadMoreVideos(roomId) },
                                                    modifier = Modifier.fillMaxWidth(0.7f)
                                                ) {
                                                    Text("더 보기")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 초기 로딩 중인 경우 로딩 표시
                            if (isVideosLoading && currentPage == 0) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 초대코드 다이얼로그
    if (showInviteCodeDialog && inviteCode.isNotEmpty()) {
        val creationTime by roomViewModel.inviteCodeCreationTime.collectAsState()
        val currentTime = System.currentTimeMillis()
        val elapsedSeconds = ((currentTime - creationTime) / 1000).toInt()
        val remainingSeconds = maxOf(0, 600 - elapsedSeconds) // 10분(600초)에서 경과 시간 차감

        InviteCodeDialog(
            inviteCode = inviteCode,
            initialRemainingSeconds = remainingSeconds, // 계산된 남은 시간 전달
            onDismiss = { showInviteCodeDialog = false },
            onCopy = {
                clipboardManager.setText(AnnotatedString(inviteCode))
                showToast("초대코드가 클립보드에 복사되었습니다")
                showInviteCodeDialog = false
            }
        )
    }

    if (showUploadDialog) {
        UploadShortDialog(
            roomId = roomId,
            onDismiss = {
                showUploadDialog = false
                resetDialogStates() // 다이얼로그가 닫힐 때 상태 초기화
            },
            onUpload = { video, uploadTitle ->
                // 업로드 처리
                scope.launch {
                    try {
                        val token = tokenManager.accessToken.first()
                        if (token != null) {
                            // 문자열 그대로 사용
                            val videoUrl = video.videoUrl  // Uri.parse() 제거
                            val thumbnailUrl = video.thumbnail  // Uri.parse() 제거

                            val videoIdStr = video.videoId.toString()

                            // 수정된 uploadShort 메서드 호출
                            roomViewModel.uploadShort(
                                roomId = roomId,
                                videoUrl = videoUrl,  // String으로 변경
                                title = uploadTitle,
                                thumbnailUrl = thumbnailUrl,  // String으로 변경
                                videoId = videoIdStr
                            )

                            showUploadDialog = false
                            resetDialogStates() // 업로드 후 상태 초기화
                        }
                    } catch (e: Exception) {
                        showToast("업로드 중 오류가 발생했습니다: ${e.message}")
                    }
                }
            },
            onVideoSelectClick = {
                // 비디오 선택 다이얼로그 표시
                showVideoSelectorDialog = true
            },
            selectedVideo = selectedVideo,
            initialTitle = title,
            onTitleChanged = { newTitle -> title = newTitle }
        )
    }

    // 비디오 선택 전체 화면 다이얼로그
    if (showVideoSelectorDialog) {
        VideoSelectorFullScreenDialog(
            myVideos = myVideos,
            onDismiss = {
                showVideoSelectorDialog = false
            },
            onVideoSelected = { video ->
                selectedVideo = video
                showVideoSelectorDialog = false
            }
        )
    }
}