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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val scrollState = rememberScrollState()

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
                // 전체 화면을 스크롤 가능하게 수정
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // 썸네일 이미지
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        // 썸네일 이미지 로드
                        if (roomDetail?.roomThumbnail != null && roomDetail?.roomThumbnail != "default_thumbnail") {
                            // 서버 URL에서 이미지 로드
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(roomDetail?.roomThumbnail)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "방 썸네일",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // 기본 이미지 표시
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.logo),
                                    contentDescription = "기본 썸네일",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }

                    // 방 정보 카드 (디자인을 이미지처럼 수정)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 방 제목과 정보 (좌우 배열)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // 방 제목
                                    Text(
                                        text = roomDetail?.roomTitle ?: "방 제목",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // 방 설명
                                    Text(
                                        text = roomDetail?.roomContext ?: "방 설명",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // 멤버 수
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "멤버 : ",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = "${roomMembers.size}명",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }


                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // 쇼츠 업로드 버튼
                                    FilledTonalButton(
                                        onClick = {
                                            showUploadDialog = true
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFE0E0E0),
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                        modifier = Modifier.width(120.dp)  // 버튼 가로 길이 120dp로 늘림
                                    ) {
                                        Text(
                                            text = "쇼츠 업로드",
                                            fontSize = 12.sp
                                        )
                                    }

                                    // 초대코드 버튼
                                    FilledTonalButton(
                                        onClick = {
                                            if (inviteCode.isNotEmpty()) {
                                                showInviteCodeDialog = true
                                            } else {
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
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFE0E0E0),
                                            contentColor = Color.Black
                                        ),
                                        shape = RoundedCornerShape(4.dp),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 8.dp
                                        ),
                                        border = null,
                                        enabled = !isLoading && !isGeneratingCode,
                                        modifier = Modifier.width(120.dp)  // 버튼 가로 길이 120dp로 늘림
                                    ) {
                                        if (isGeneratingCode) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            Text(
                                                text = "초대 코드",
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // 비디오 목록 제목
                    Text(
                        text = "쇼츠 목록",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )

                    // 비디오 목록 표시 방식 변경 - LazyVerticalGrid 대신 일반 Column으로 대체
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
                                Text(
                                    text = "업로드된 쇼츠가 없습니다",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // 비디오 목록 표시 - 그리드 형태로 배치
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            // 항목을 2개씩 묶어서 행으로 표시
                            val videoChunks = roomVideos.chunked(2)

                            videoChunks.forEach { rowVideos ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowVideos.forEach { video ->
                                        Box(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            RoomVideoItem(
                                                video = video,
                                                onClick = {
                                                    navController.navigate("room_video_detail/${roomId}/${video.id}")
                                                }
                                            )
                                        }
                                    }

                                    // 한 행에 비디오가 1개만 있는 경우 빈 공간 추가
                                    if (rowVideos.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            // 더 불러오기 버튼
                            if (hasMoreVideos) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    // 하단 여백 추가
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // 초대코드 다이얼로그 코드는 그대로 유지
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

    // 업로드 다이얼로그 코드는 그대로 유지
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

    // 비디오 선택 전체 화면 다이얼로그 코드는 그대로 유지
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