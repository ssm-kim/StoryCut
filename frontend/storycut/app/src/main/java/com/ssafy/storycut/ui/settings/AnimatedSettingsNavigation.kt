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
    navController: NavController,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth }, // 오른쪽에서 시작
            animationSpec = tween(durationMillis = 300) // 애니메이션 시간
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth }, // 오른쪽으로 사라짐
            animationSpec = tween(durationMillis = 300)
        )
    ) {
        SettingsScreen(
            navController = navController,
            authViewModel = authViewModel,
            onBackPressed = onDismiss
        )
    }
}