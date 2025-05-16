package com.ssafy.storycut.ui.room.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ssafy.storycut.data.api.model.VideoDto

@Composable
fun UploadShortDialog(
    roomId: String,
    onDismiss: () -> Unit,
    onUpload: (VideoDto, String) -> Unit,
    onVideoSelectClick: () -> Unit,
    selectedVideo: VideoDto?,
    initialTitle: String = "",
    onTitleChanged: (String) -> Unit = {}
) {
    var title by remember { mutableStateOf(initialTitle) }

    // 다이얼로그가 표시될 때마다 초기화
    DisposableEffect(Unit) {
        // 초기 상태 설정
        title = initialTitle

        // 다이얼로그가 닫힐 때 정리
        onDispose { }
    }

    // 제목이 변경될 때 콜백 호출
    LaunchedEffect(title) {
        onTitleChanged(title)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "쇼츠 업로드",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 영상 선택 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onVideoSelectClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedVideo != null) {
                        // 선택된 영상 표시
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedVideo.thumbnail)
                                .crossfade(true)
                                .build(),
                            contentDescription = "선택된 쇼츠",
                            contentScale = ContentScale.Fit, // Fit으로 변경하여 이미지가 잘리지 않도록 함
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // 영상 선택 안내
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "영상 선택",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(8.dp),
                                tint = Color.Gray
                            )

                            Text(
                                text = "영상을 선택하세요",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // 제목 입력 필드
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    placeholder = { Text("쇼츠 제목을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    singleLine = true,
                    maxLines = 1
                )

                // 버튼 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "취소")
                    }

                    Button(
                        onClick = {
                            if (selectedVideo != null && title.isNotBlank()) {
                                onUpload(selectedVideo, title)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedVideo != null && title.isNotBlank()
                    ) {
                        Text(text = "업로드")
                    }
                }
            }
        }
    }
}