package com.ssafy.storycut.ui.edit

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import androidx.compose.ui.draw.shadow
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
import com.ssafy.storycut.ui.edit.dialog.VideoUploadDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var showVideoDialog by remember { mutableStateOf(false) }
    var showOptionDialog by remember { mutableStateOf(false) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var videoThumbnail by remember { mutableStateOf<Bitmap?>(null) }
    var videoSelected by remember { mutableStateOf(false) }

    // 선택된 옵션들 상태
    var hasMosaic by remember { mutableStateOf(false) }
    var hasKoreanSubtitle by remember { mutableStateOf(false) }
    var hasBackgroundMusic by remember { mutableStateOf(false) }

    // 프롬프트 상태
    var promptText by remember { mutableStateOf("") }

    // 모자이크 대상 이미지 리스트 상태
    var mosaicImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

    // 갤러리에서 비디오 선택을 위한 런처
    val galleryVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            videoSelected = true

            // 비디오 썸네일 추출 (IO 쓰레드에서 실행)
            coroutineScope.launch {
                val bitmap = withContext(Dispatchers.IO) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(context, uri)
                        // 첫 프레임 가져오기
                        val bitmap = retriever.getFrameAtTime(0)
                        retriever.release()
                        bitmap
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
                videoThumbnail = bitmap
            }
        }
    }

    // 비디오 선택 함수들
    fun openGalleryVideos() {
        galleryVideoLauncher.launch("video/*")
        showVideoDialog = false
    }

    fun openMyShorts() {
        // 실제 구현에서는 앱 내부의 쇼츠 비디오를 로드하는 로직이 필요
        galleryVideoLauncher.launch("video/*")
        showVideoDialog = false
    }

    // 옵션 적용 함수들
    fun applyMosaic() {
        hasMosaic = true
    }

    fun addKoreanSubtitle() {
        hasKoreanSubtitle = true
    }

    fun addBackgroundMusic() {
        hasBackgroundMusic = true
    }

    // 옵션 제거 함수들
    fun removeMosaic() {
        hasMosaic = false
        mosaicImages = emptyList() // 모자이크 제거 시 이미지도 초기화
    }

    fun removeKoreanSubtitle() {
        hasKoreanSubtitle = false
    }

    fun removeBackgroundMusic() {
        hasBackgroundMusic = false
    }

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
            if (videoSelected && videoThumbnail != null) {
                // 비디오 썸네일 표시
                Image(
                    bitmap = videoThumbnail!!.asImageBitmap(),
                    contentDescription = "선택된 비디오",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (videoSelected) {
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
        if (hasMosaic) {
            OptionalSection(
                title = "모자이크",
                onRemove = { removeMosaic() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("제외할 인물을 올려 주세요", fontSize = 12.sp, color = Color.Gray)

                    val photoPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        if (uri != null && mosaicImages.size < 2) {
                            mosaicImages = mosaicImages + uri
                        }
                    }

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
                                    if (mosaicImages.size < 2) {
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
                        mosaicImages.forEachIndexed { index, uri ->
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
                                            mosaicImages = mosaicImages.filterIndexed { i, _ -> i != index }
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
        if (hasKoreanSubtitle) {
            OptionalSection(
                title = "한국어 자막",
                onRemove = { removeKoreanSubtitle() }
            ) {
                // 자막 설정 영역 내용
            }

            HorizontalDivider(color = Color.LightGray)
        }

        // 배경 음악 옵션 (조건부 표시)
        if (hasBackgroundMusic) {
            OptionalSection(
                title = "배경 음악",
                onRemove = { removeBackgroundMusic() }
            ) {
                // 배경 음악 설정 영역 내용
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    placeholder = { Text("프롬프트") },
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
            enabled = !(hasMosaic && hasKoreanSubtitle && hasBackgroundMusic) // 모든 옵션이 추가된 경우 비활성화
        ) {
            Text(
                "옵션추가",
                color = if (hasMosaic && hasKoreanSubtitle && hasBackgroundMusic) Color.Gray else Color.Black
            )
        }

        // 프롬프트 입력 영역
        OutlinedTextField(
            value = promptText,
            onValueChange = { promptText = it },
            placeholder = { Text("프롬프트") },
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
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("편집하기", color = Color.White)
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
            onMosaicClick = { applyMosaic() },
            onKoreanSubtitleClick = { addKoreanSubtitle() },
            onBackgroundMusicClick = { addBackgroundMusic() },
            hasMosaic = hasMosaic,
            hasKoreanSubtitle = hasKoreanSubtitle,
            hasBackgroundMusic = hasBackgroundMusic
        )
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
