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
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ssafy.storycut.ui.edit.EditViewModel
import com.ssafy.storycut.ui.mypage.VideoDetailScreen
import com.ssafy.storycut.ui.room.RoomDetailScreen

/**
 * 메인 화면의 네비게이션 그래프를 확장 함수로 정의
 */
@OptIn(UnstableApi::class)
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    tokenManager: TokenManager,
    onNavigateToLogin: () -> Unit
) {
    composable(Navigation.Main.HOME) {
        HomeScreen(
            onRoomClick = { roomId ->
                navController.navigate("room_detail/$roomId")
            }
        )
    }

    composable(Navigation.Main.EDIT) {
        // hiltViewModel()을 사용하여 EditViewModel 인스턴스 생성
        val editViewModel = hiltViewModel<EditViewModel>()

        EditScreen(
            viewModel = editViewModel,
            onEditSuccess = { videoId ->
                // 편집 성공 시 비디오 상세 화면으로 이동
                navController.navigate("video_detail/$videoId")
            }
        )
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
        route = "room_detail/{roomId}",
        arguments = listOf(navArgument("roomId") { type = NavType.StringType })
    ) { backStackEntry ->
        val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
        RoomDetailScreen(
            roomId = roomId,
            navController = navController,
            tokenManager = tokenManager
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
