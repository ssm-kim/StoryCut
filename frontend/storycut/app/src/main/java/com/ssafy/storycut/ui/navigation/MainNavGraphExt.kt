package com.ssafy.storycut.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.auth.AuthViewModel
import com.ssafy.storycut.ui.edit.EditScreen
import com.ssafy.storycut.ui.home.HomeScreen
import com.ssafy.storycut.ui.mypage.MyPageScreen
import com.ssafy.storycut.ui.mypage.VideoViewModel
import com.ssafy.storycut.ui.settings.SettingsScreen
import ShortsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ssafy.storycut.ui.mypage.VideoDetailScreen

/**
 * 메인 화면의 네비게이션 그래프를 확장 함수로 정의
 */
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    tokenManager: TokenManager,
    onNavigateToLogin: () -> Unit
) {
    composable(Navigation.Main.HOME) {
        HomeScreen()
    }

    composable(Navigation.Main.EDIT) {
        EditScreen()
    }

    composable(Navigation.Main.SHORTS_UPLOAD) {
        ShortsScreen()
    }

    composable(Navigation.Main.MYPAGE) {
        // hiltViewModel()을 사용하여 VideoViewModel 인스턴스 생성
        val myVideoViewModel = hiltViewModel<VideoViewModel>()

        MyPageScreen(
            authViewModel = authViewModel,
            myVideoViewModel = myVideoViewModel,
            tokenManager = tokenManager,
            navController = navController,
            onNavigateToLogin = onNavigateToLogin  // 이 부분이 올바르게 전달되고 있는지 확
        )
    }

    composable(Navigation.Main.SETTINGS) {
        SettingsScreen(
            authViewModel = authViewModel,
            onBackPressed = { navController.popBackStack() },
            onNavigateToLogin = onNavigateToLogin // 콜백 전달
        )
    }

    composable(
        route = "video_detail/{videoId}",
        arguments = listOf(navArgument("videoId") { type = NavType.StringType })
    ) { backStackEntry ->
        val videoId = backStackEntry.arguments?.getString("videoId") ?: ""
        VideoDetailScreen(
            videoId = videoId,
            navController = navController,
            tokenManager = tokenManager
        )
    }
}