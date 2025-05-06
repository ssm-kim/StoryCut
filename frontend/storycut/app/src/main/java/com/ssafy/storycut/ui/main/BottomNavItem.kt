package com.ssafy.storycut.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Create
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 하단 내비게이션 바의 아이템을 정의하는 클래스
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // 홈 화면 아이템
    object Home : BottomNavItem(
        route = "home",
        title = "홈",
        icon = Icons.Default.Home
    )

    // 영상 편집 아이템
    object Edit : BottomNavItem(
        route = "edit",
        title = "영상편집",
        icon = Icons.Default.List
    )

    // 쇼츠 업로드 아이템
    object Shorts : BottomNavItem(
        route = "shorts",
        title = "쇼츠 업로드",
        icon = Icons.Default.Create
    )

    // 마이페이지 아이템
    object MyPage : BottomNavItem(
        route = "mypage",
        title = "마이페이지",
        icon = Icons.Default.Person
    )
}