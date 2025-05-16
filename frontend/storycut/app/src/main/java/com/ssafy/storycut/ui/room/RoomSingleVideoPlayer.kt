package com.ssafy.storycut.ui.room

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.ssafy.storycut.R
import com.ssafy.storycut.data.api.model.chat.ChatDto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@UnstableApi
@Composable
fun RoomSingleVideoPlayer(
    video: ChatDto,
    isCurrentlyVisible: Boolean,
    onPlayerCreated: (ExoPlayer) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(isCurrentlyVisible) }

    // 포맷된 날짜 상태
    val formattedDate = remember(video.createdAt) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault())
            val date = inputFormat.parse(video.createdAt) ?: Date()
            outputFormat.format(date)
        } catch (e: Exception) {
            video.createdAt
        }
    }

    // 플레이어 상태 관리
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = isCurrentlyVisible
            repeatMode = Player.REPEAT_MODE_ONE  // 비디오 반복 재생

            // 비디오 URL 설정
            val mediaItem = MediaItem.fromUri(video.mediaUrl)
            setMediaItem(mediaItem)
            prepare()

            // 플레이어 콜백 호출
            onPlayerCreated(this)
        }
    }

    // 화면이 종료되면 ExoPlayer 해제
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 현재 보이는 상태 변경 감지
    LaunchedEffect(isCurrentlyVisible) {
        if (isCurrentlyVisible) {
            exoPlayer.play()
            isPlaying = true
        } else {
            exoPlayer.pause()
            isPlaying = false
        }
    }

    Box(
        modifier = modifier
            .clickable {
                // 화면 클릭 시 재생/일시정지 토글
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    isPlaying = false
                } else {
                    exoPlayer.play()
                    isPlaying = true
                }
            }
    ) {
        // 비디오 플레이어
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false  // 기본 컨트롤러 숨기기
                    setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 재생/정지 아이콘 (정지 상태일 때만 표시)
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "재생",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // 비디오 정보 (하단에 표시)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 작성자 정보 (프로필 이미지와 함께 표시)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 프로필 이미지 추가
                    AsyncImage(
                        model = R.drawable.ic_launcher_foreground,
                        contentDescription = "작성자 프로필",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        error = painterResource(id = R.drawable.ic_launcher_foreground)
                    )

                    Spacer(modifier = Modifier.padding(start = 10.dp))

                    // 작성자 이름
                    Text(
                        text = "사용자",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 비디오 제목
                Text(
                    text = video.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 날짜 정보
                Text(
                    text = formattedDate,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        }
    }
}