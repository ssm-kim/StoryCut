package com.ssafy.storycut.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ssafy.storycut.ui.auth.AuthViewModel
import com.ssafy.storycut.ui.edit.EditScreen
import com.ssafy.storycut.ui.home.HomeScreen
import com.ssafy.storycut.ui.mypage.MyPageScreen
import com.ssafy.storycut.ui.shorts.ShortsScreen

@Composable
fun MainScreen(authViewModel: AuthViewModel = hiltViewModel()) {
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
                                navController.navigate(item.route) {
                                    // 네비게이션 컨트롤러의 스택을 초기화하여 백 스택 관리
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // 같은 아이템을 여러 번 클릭했을 때 같은 화면의 여러 인스턴스가 쌓이는 것을 방지
                                    launchSingleTop = true
                                    // 이전 상태를 복원
                                    restoreState = true
                                }
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
                startDestination = BottomNavItem.Home.route // 시작 화면을 홈으로 변경
            ) {
                // 각 화면에 대한 컴포저블 정의
                composable(BottomNavItem.Home.route) {
                    HomeScreen()
                }
                composable(BottomNavItem.Edit.route) {
                    EditScreen()
                }
                composable(BottomNavItem.Shorts.route) {
                    ShortsScreen()
                }
                composable(BottomNavItem.MyPage.route) {
                    // 마이페이지에 MainScreen에서 받은 AuthViewModel과 NavController 전달
                    MyPageScreen(
                        authViewModel = authViewModel,
                        navController = navController
                    )
                }
            }
        }
    }
}