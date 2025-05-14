//package com.ssafy.storycut.ui.room.upload
//
//import android.net.Uri
//import android.widget.Toast
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.NavController
//import com.ssafy.storycut.ui.room.RoomViewModel
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RoomShortUploadScreen(
//    roomId: String,
//    navController: NavController,
//    viewModel: RoomViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//    val isLoading by viewModel.isLoading.collectAsState()
//    val error by viewModel.error.collectAsState()
//    val uploadSuccess by viewModel.uploadSuccess.collectAsState()
//
//    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
//    var title by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var tags by remember { mutableStateOf("") }
//
//    val scrollState = rememberScrollState()
//
//    // 갤러리에서 영상 선택
//    val videoPickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent()
//    ) { uri: Uri? ->
//        selectedVideoUri = uri
//    }
//
//    // 에러 메시지 표시
//    LaunchedEffect(error) {
//        if (error.isNotEmpty()) {
//            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
//            viewModel.clearError()
//        }
//    }
//
//    // 업로드 성공 시 이전 화면으로 돌아가기
//    LaunchedEffect(uploadSuccess) {
//        if (uploadSuccess) {
//            Toast.makeText(context, "쇼츠가 성공적으로 업로드되었습니다.", Toast.LENGTH_SHORT).show()
//            navController.popBackStack()
//        }
//    }
//
//    // 화면이 사라질 때 상태 초기화
//    DisposableEffect(Unit) {
//        onDispose {
//            viewModel.resetUploadState()
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("쇼츠 업로드") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            imageVector = Icons.Default.ArrowBack,
//                            contentDescription = "뒤로 가기"
//                        )
//                    }
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            )
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp)
//                    .verticalScroll(scrollState),
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // 비디오 선택 섹션
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.surface
//                    ),
//                    elevation = CardDefaults.cardElevation(
//                        defaultElevation = 2.dp
//                    )
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Text(
//                            text = "비디오 선택",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//
//                        Button(
//                            onClick = { videoPickerLauncher.launch("video/*") },
//                            modifier = Modifier.fillMaxWidth(),
//                            enabled = !isLoading
//                        ) {
//                            Text(text = if (selectedVideoUri == null) "갤러리에서 비디오 선택하기" else "다른 비디오 선택하기")
//                        }
//
//                        // 선택된 비디오 정보 표시
//                        selectedVideoUri?.let {
//                            Text(
//                                text = "선택된 비디오: ${it.lastPathSegment ?: "알 수 없음"}",
//                                style = MaterialTheme.typography.bodyMedium
//                            )
//                        }
//                    }
//                }
//
//                // 비디오 정보 입력 섹션
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.surface
//                    ),
//                    elevation = CardDefaults.cardElevation(
//                        defaultElevation = 2.dp
//                    )
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        Text(
//                            text = "비디오 정보",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//
//                        // 제목 입력
//                        OutlinedTextField(
//                            value = title,
//                            onValueChange = { title = it },
//                            label = { Text("제목") },
//                            modifier = Modifier.fillMaxWidth(),
//                            enabled = !isLoading,
//                            singleLine = true
//                        )
//
//                        // 설명 입력
//                        OutlinedTextField(
//                            value = description,
//                            onValueChange = { description = it },
//                            label = { Text("설명") },
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(120.dp),
//                            enabled = !isLoading
//                        )
//
//                        // 태그 입력
//                        OutlinedTextField(
//                            value = tags,
//                            onValueChange = { tags = it },
//                            label = { Text("태그 (쉼표로 구분)") },
//                            placeholder = { Text("예: 여행, 일상, 브이로그") },
//                            modifier = Modifier.fillMaxWidth(),
//                            enabled = !isLoading
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // 업로드 버튼
//                Button(
//                    onClick = {
//                        selectedVideoUri?.let { uri ->
//                            viewModel.uploadShort(
//                                roomId = roomId,
//                                videoUri = uri,
//                                title = title.ifBlank { "스토리컷에서 올린 영상" },
//                                description = description.ifBlank { "앱에서 자동 업로드된 영상입니다" },
//                                tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
//                            )
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(50.dp),
//                    enabled = selectedVideoUri != null && !isLoading
//                ) {
//                    Text("업로드하기")
//                }
//            }
//
//            // 로딩 인디케이터
//            if (isLoading) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Surface(
//                        modifier = Modifier,
//                        shape = MaterialTheme.shapes.medium,
//                        tonalElevation = 8.dp,
//                        shadowElevation = 8.dp
//                    ) {
//                        Column(
//                            modifier = Modifier.padding(24.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            CircularProgressIndicator()
//                            Text("업로드 중...", style = MaterialTheme.typography.bodyMedium)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}