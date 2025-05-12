package com.ssafy.storycut.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ssafy.storycut.R
import com.ssafy.storycut.data.api.model.room.RoomDto
import com.ssafy.storycut.ui.home.dialog.CreateRoomDialog
import com.ssafy.storycut.ui.home.dialog.EnterRoomDialog
import com.ssafy.storycut.ui.home.dialog.RoomOptionsDialog

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onRoomClick: (String) -> Unit = {},
    onCreateRoomClick: () -> Unit = {},
    onEnterRoomClick: () -> Unit = {}
) {
    // ViewModel에서 방 목록 데이터 가져오기
    val myRooms by viewModel.myRooms.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val error by viewModel.error.observeAsState("")

    // 새로 생성된 방 ID 관찰 (추가된 부분)
    val createdRoomId by viewModel.createdRoomId.observeAsState("")
    val enteredRoomId by viewModel.enteredRoomId.observeAsState("")

    // 다이얼로그 표시 상태
    var showRoomOptionsDialog by remember { mutableStateOf(false) }
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    var showEnterRoomDialog by remember { mutableStateOf(false) }

    // 새로 생성된 방으로 자동 이동 (추가된 부분)
    LaunchedEffect(createdRoomId) {
        if (createdRoomId.isNotEmpty()) {
            // 방으로 이동
            onRoomClick(createdRoomId)
            // ID 초기화
            viewModel.clearCreatedRoomId()
        }
    }

    // 입장한 방으로 자동 이동 (추가된 부분)
    LaunchedEffect(enteredRoomId) {
        if (enteredRoomId.isNotEmpty()) {
            // 방으로 이동
            onRoomClick(enteredRoomId)
            // ID 초기화
            viewModel.clearEnteredRoomId()
        }
    }

    // 컴포넌트가 처음 표시될 때 데이터 로드
    LaunchedEffect(key1 = true) {
        viewModel.getMyRooms()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 상단 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Room",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )

            // 방 추가 버튼
            IconButton(
                onClick = { showRoomOptionsDialog = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "방 추가",
                    tint = Color.Gray
                )
            }
        }

        // 로딩 상태 표시
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error.isNotEmpty()) {
            // 에러 메시지 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            // 방 목록 표시
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (myRooms.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "참가중인 공유방이 없습니다",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // 참가중인 방 목록
                    item {
                        Text(
                            text = "참가중인방",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(myRooms) { room ->
                        RoomItem(
                            room = room,
                            onClick = { onRoomClick(room.roomId.toString()) }
                        )
                    }
                }
            }
        }
    }

    // 방 옵션 다이얼로그
    if (showRoomOptionsDialog) {
        RoomOptionsDialog(
            onDismiss = { showRoomOptionsDialog = false },
            onCreateRoom = {
                showRoomOptionsDialog = false
                showCreateRoomDialog = true
            },
            onEnterRoom = {
                showRoomOptionsDialog = false
                showEnterRoomDialog = true
            }
        )
    }

    // 방 생성 다이얼로그
    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateRoomDialog = false },
            onCreateRoom = { request, imageUri ->
                viewModel.createRoom(request, imageUri)
                showCreateRoomDialog = false // 다이얼로그 닫기
            }
        )
    }

    // 방 입장 다이얼로그
    if (showEnterRoomDialog) {
        EnterRoomDialog(
            onDismiss = { showEnterRoomDialog = false },
            onEnterRoom = { inviteCode ->
                viewModel.enterRoom(inviteCode)
                showEnterRoomDialog = false // 다이얼로그 닫기
            }
        )
    }
}@Composable
fun RoomItem(
    room: RoomDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 썸네일 이미지
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                // 썸네일 이미지 로드
                if (room.roomThumbnail != null && room.roomThumbnail != "default_thumbnail") {
                    // FastAPI에서 제공한 URL로 이미지 로드
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(room.roomThumbnail)
                            .crossfade(true)
                            .build(),
                        contentDescription = "방 썸네일",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // 기본 이미지 표시
                    Image(
                        painter = painterResource(id = R.drawable.logo), // 적절한 기본 이미지 리소스 필요
                        contentDescription = "기본 썸네일",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // 방 정보 표시
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = room.roomTitle ?: "EPL 명장면 쇼츠",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "쇼츠 • 롱징",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Text(
                    text = "${room.memberCount ?: 0}명",
                    fontSize = 14.sp
                )
            }
        }
    }
}