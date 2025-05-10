package com.ssafy.storycut.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.auth.AuthViewModel

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    tokenManager: TokenManager,
    onNavigateToLogin: () -> Unit = {}
) {
    val navController = rememberNavController()

    // 내비게이션 아이템 리스트
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Edit,
        BottomNavItem.Shorts,
        BottomNavItem.MyPage
    )

    // 현재 선택된 탭 정보 가져오기
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(text = item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            // 현재 선택된 아이템이 아닌 경우에만 네비게이션 처리
                            if (currentRoute != item.route) {
                                // navigateToMainTab 함수 사용
                                navController.navigateToMainTab(item.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // 내비게이션 호스트 설정
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Navigation.Main.HOME
            ) {
                mainGraph(
                    navController = navController,
                    authViewModel = authViewModel,
                    tokenManager = tokenManager,
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}

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
        route = Navigation.Main.HOME,
        title = "홈",
        icon = Icons.Default.Home
    )

    // 영상 편집 아이템
    object Edit : BottomNavItem(
        route = Navigation.Main.EDIT,
        title = "영상편집",
        icon = Icons.Default.List
    )

    // 쇼츠 업로드 아이템
    object Shorts : BottomNavItem(
        route = Navigation.Main.SHORTS_UPLOAD,
        title = "쇼츠 업로드",
        icon = Icons.Default.Create
    )

    // 마이페이지 아이템
    object MyPage : BottomNavItem(
        route = Navigation.Main.MYPAGE,
        title = "마이페이지",
        icon = Icons.Default.Person
    )
}