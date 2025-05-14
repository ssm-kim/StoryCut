package com.ssafy.storycut.ui.room.upload

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ssafy.storycut.ui.common.VideoUploadDialog
    import com.ssafy.storycut.ui.room.RoomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomShortUploadScreen(
    roomId: String,
    navController: NavController,
    viewModel: RoomViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val uploadSuccess by viewModel.uploadSuccess.collectAsState()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showVideoUploadDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // 갤러리에서 영상 선택
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedVideoUri = uri
    }

    // 에러 메시지 표시
    LaunchedEffect(error) {
        if (error.isNotEmpty()) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // 업로드 성공 시 이전 화면으로 돌아가기
    LaunchedEffect(uploadSuccess) {
        if (uploadSuccess) {
            Toast.makeText(context, "쇼츠가 성공적으로 업로드되었습니다.", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    // 화면이 사라질 때 상태 초기화
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetUploadState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("쇼츠 업로드") },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 비디오 선택 영역
                if (selectedVideoUri == null) {
                    // 비디오가 선택되지 않은 경우 - + 아이콘 카드
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clickable(onClick = { showVideoUploadDialog = true })
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "비디오 추가",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "쇼츠 비디오 선택하기",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    // 선택된 비디오 정보 카드
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "선택된 비디오",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = selectedVideoUri.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // 다른 비디오 선택 버튼
                            OutlinedButton(
                                onClick = { showVideoUploadDialog = true },
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text("다른 비디오 선택하기")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 제목 입력 필드
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("쇼츠 제목") },
                    placeholder = { Text("제목을 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 설명 입력 필드
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("쇼츠 설명") },
                    placeholder = { Text("설명을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = !isLoading,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 업로드 버튼
                Button(
                    onClick = {
                        selectedVideoUri?.let { uri ->
                            viewModel.uploadShort(
                                roomId = roomId,
                                videoUri = uri,
                                title = title.ifBlank { "스토리컷에서 올린 영상" }
                            )
                        }
                    },
                    enabled = !isLoading && selectedVideoUri != null && title.isNotBlank(),  // 비디오 선택 및 제목 입력 필요
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("공유방에 쇼츠 업로드하기")
                }
            }

            // 로딩 인디케이터
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // 비디오 업로드 다이얼로그
            if (showVideoUploadDialog) {
                VideoUploadDialog(
                    onDismiss = { showVideoUploadDialog = false },
                    onGalleryClick = {
                        videoPickerLauncher.launch("video/*")
                        showVideoUploadDialog = false
                    },
                    onShortsClick = {
                        // 내 쇼츠에서 선택하는 로직 추가 필요
                        // 예: navController.navigate("my_shorts") 또는 별도 API 호출
                        Toast.makeText(context, "내 쇼츠 기능은 준비 중입니다", Toast.LENGTH_SHORT).show()
                        showVideoUploadDialog = false
                    }
                )
            }
        }
    }
}