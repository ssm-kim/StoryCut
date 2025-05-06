package com.ssafy.storycut.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.ssafy.storycut.R
import com.ssafy.storycut.ui.auth.AuthViewModel

@Composable
fun MyPageScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    navController: NavController? = null
//    postViewModel: PostViewModel = hiltViewModel()
) {
    val userInfo by authViewModel.userState.collectAsState()
//    val postList by postViewModel.postList.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        authViewModel.refreshUserInfoFromRoom()
//        postViewModel.fetchMyPosts()
    }
    
    // userInfo가 null인 경우 로딩 상태 표시
    if (userInfo == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "사용자 정보를 불러오는 중...")
            }
        }
        return
    }

    // userInfo가 null이 아닌 경우에만 아래 코드 실행
    Column(modifier = Modifier.fillMaxSize()) {
        // 설정 아이콘
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Icon(
                painter = painterResource(id = R.drawable.setting),
                contentDescription = "설정",
                modifier = Modifier.size(24.dp)
            )
        }

        // 프로필 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = userInfo?.profileImg,
                contentDescription = "프로필 이미지",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = userInfo?.name ?: "사용자", fontWeight = FontWeight.Bold)
                Text(text = userInfo?.email ?: "이메일 없음", style = MaterialTheme.typography.bodySmall)
//                Text(text = "게시글 : ${postList.size}개")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 검색창
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("사진 검색하기") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

//        // 게시물 섹션
//        if (postList.isEmpty()) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(32.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = "첫 쇼츠를 업로드해 보세요",
//                    style = MaterialTheme.typography.bodyMedium,
//                    textAlign = TextAlign.Center
//                )
//            }
//        } else {
//            LazyVerticalGrid(
//                columns = GridCells.Fixed(3),
//                modifier = Modifier.fillMaxSize(),
//                contentPadding = PaddingValues(4.dp)
//            ) {
//                items(postList) { post ->
//                    Image(
//                        painter = rememberAsyncImagePainter(model = post.thumbnailUrl),
//                        contentDescription = null,
//                        modifier = Modifier
//                            .aspectRatio(1f)
//                            .padding(2.dp)
//                            .clip(MaterialTheme.shapes.small)
//                    )
//                }
//            }
//        }
    }
}
