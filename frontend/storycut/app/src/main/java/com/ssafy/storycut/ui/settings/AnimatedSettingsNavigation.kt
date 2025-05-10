package com.ssafy.storycut.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.ssafy.storycut.ui.auth.AuthViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedSettingsNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onNavigateToLogin: () -> Unit = {} // 올바른 람다 함수 기본값
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
        SettingsScreen(
            authViewModel = authViewModel,
            onBackPressed = onDismiss,
            onNavigateToLogin = onNavigateToLogin // 콜백 전달
        )
    }
}