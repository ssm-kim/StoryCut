package com.ssafy.storycut.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssafy.storycut.ui.auth.AuthViewModel

@Composable
fun AnimatedSettingsNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit = {}
) {
    // 시스템 뒤로가기 버튼 처리
    BackHandler(enabled = isVisible) {
        onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            // 설정 화면 표시
            SettingsScreen(
                authViewModel = authViewModel,
                onBackPressed = onDismiss, // 앱 내 뒤로가기 버튼 클릭 시 onDismiss 콜백 호출
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}