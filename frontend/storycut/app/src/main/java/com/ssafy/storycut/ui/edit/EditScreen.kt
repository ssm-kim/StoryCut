package com.ssafy.storycut.ui.edit


import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ssafy.storycut.R
import com.ssafy.storycut.ui.edit.dialog.OptionDialog
import com.ssafy.storycut.ui.common.VideoUploadDialog
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EditScreen(
    viewModel: EditViewModel,
    onEditSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showVideoDialog by remember { mutableStateOf(false) }
    var showOptionDialog by remember { mutableStateOf(false) }

    // 오류 메시지 표시를 위한 효과
    LaunchedEffect(viewModel.error) {
        viewModel.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // 이벤트 수집
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is EditViewModel.EditEvent.Success -> {
                    onEditSuccess(event.videoId)
                }
                is EditViewModel.EditEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // 갤러리에서 비디오 선택을 위한 런처
    val galleryVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setSelectedVideo(it) }
    }

    // 갤러리에서 이미지 선택을 위한 런처
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.addMosaicImage(it) }
    }

    // 비디오 선택 함수들
    fun openGalleryVideos() {
        galleryVideoLauncher.launch("video/*")
        showVideoDialog = false
    }

    fun openMyShorts() {
        galleryVideoLauncher.launch("video/*")
        showVideoDialog = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 상단 타이틀
            Text(
                text = "영상 편집",
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // 동영상 업로드 레이블
            Text(
                text = "동영상 업로드",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )

            // 동영상 업로드 영역 - 선택된 비디오 썸네일 표시 또는 + 아이콘
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .border(2.dp, Color(0xFF2196F3), RoundedCornerShape(4.dp))
                    .clickable { showVideoDialog = true },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.videoSelected && viewModel.videoThumbnail != null) {
                    // 비디오 썸네일 표시
                    Image(
                        bitmap = viewModel.videoThumbnail!!.asImageBitmap(),
                        contentDescription = "선택된 비디오",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (viewModel.videoSelected) {
                    // 비디오는 선택됐지만 썸네일이 아직 로드되지 않은 경우
                    CircularProgressIndicator()
                } else {
                    // 비디오가 선택되지 않은 경우 + 아이콘 표시
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "동영상 추가",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Black
                    )
                }
            }

            // 구분선
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray
            )

            // 모자이크 옵션 (조건부 표시)
            if (viewModel.hasMosaic) {
                OptionalSection(
                    title = "모자이크",
                    onRemove = { viewModel.toggleMosaic(false) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("제외할 인물을 올려 주세요", fontSize = 12.sp, color = Color.Gray)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // + 아이콘 (사진 추가 버튼) - 점선 테두리로 변경
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .drawWithContent {
                                        // 점선 테두리 그리기
                                        drawContent()
                                        val stroke = Stroke(
                                            width = 1f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                        drawRoundRect(
                                            color = Color.LightGray,
                                            style = stroke,
                                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                        )
                                    }
                                    .clickable {
                                        if (viewModel.mosaicImages.size < 2) {
                                            photoPickerLauncher.launch("image/*")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                // 회색 + 아이콘
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "인물 사진 추가",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            // 선택된 인물 사진들
                            viewModel.mosaicImages.forEachIndexed { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                ) {
                                    // 사진 - 수정자 추가
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(uri)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "인물 ${index + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    // 삭제 버튼
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .background(Color.White, CircleShape)
                                            .clickable {
                                                viewModel.removeMosaicImage(index)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.remove),
                                            contentDescription = "사진 삭제",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = Color.LightGray)
            }

            // 한국어 자막 옵션 (조건부 표시)
            if (viewModel.hasKoreanSubtitle) {
                OptionalSection(
                    title = "한국어 자막",
                    onRemove = { viewModel.toggleKoreanSubtitle(false) }
                ) {
                    // 자막 설정 영역 내용
                    Text(
                        "한국어 자막이 자동으로 생성됩니다.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                HorizontalDivider(color = Color.LightGray)
            }

            // 배경 음악 옵션 (조건부 표시)
            if (viewModel.hasBackgroundMusic) {
                OptionalSection(
                    title = "배경 음악",
                    onRemove = { viewModel.toggleBackgroundMusic(false) }
                ) {
                    // 배경 음악 설정 영역 내용
                    OutlinedTextField(
                        value = viewModel.promptText,
                        onValueChange = { viewModel.updatePromptText(it) },
                        placeholder = { Text("음악 분위기를 설명해주세요 (예: 신나는, 슬픈, 로맨틱한)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray,
                            focusedBorderColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                HorizontalDivider(color = Color.LightGray)
            }

            // 옵션추가 버튼 - 모든 옵션이 추가된 경우 비활성화
            val allOptionsAdded = viewModel.hasMosaic && viewModel.hasKoreanSubtitle && viewModel.hasBackgroundMusic
            Button(
                onClick = { showOptionDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFBBBBBB),
                    disabledContainerColor = Color(0xFFDDDDDD)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !allOptionsAdded // 모든 옵션이 추가된 경우 비활성화
            ) {
                Text(
                    "옵션추가",
                    color = if (allOptionsAdded) Color.Gray else Color.Black
                )
            }

            // 프롬프트 입력 영역
            OutlinedTextField(
                value = viewModel.promptText,
                onValueChange = { viewModel.updatePromptText(it) },
                placeholder = { Text("영상에 대한 프롬프트를 입력하세요") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 편집하기 버튼
            Button(
                onClick = { viewModel.processEditing() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !viewModel.isLoading && viewModel.videoSelected // 로딩 중이 아니고 비디오가 선택된 경우만 활성화
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("편집하기", color = Color.White)
                }
            }
        }
    }

    // 비디오 업로드 다이얼로그 표시
    if (showVideoDialog) {
        VideoUploadDialog(
            onDismiss = { showVideoDialog = false },
            onGalleryClick = { openGalleryVideos() },
            onShortsClick = { openMyShorts() }
        )
    }

    // 옵션 다이얼로그 표시
    if (showOptionDialog) {
        OptionDialog(
            onDismiss = { showOptionDialog = false },
            onMosaicClick = { viewModel.toggleMosaic(true) },
            onKoreanSubtitleClick = { viewModel.toggleKoreanSubtitle(true) },
            onBackgroundMusicClick = { viewModel.toggleBackgroundMusic(true) },
            hasMosaic = viewModel.hasMosaic,
            hasKoreanSubtitle = viewModel.hasKoreanSubtitle,
            hasBackgroundMusic = viewModel.hasBackgroundMusic
        )
    }

    // 로딩 오버레이
    if (viewModel.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    "영상 처리 중...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OptionalSection(
    title: String,
    onRemove: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 제목 및 제거 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            // 제거 버튼
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.remove),
                    contentDescription = "제거",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 섹션 내용
        content()
    }
}