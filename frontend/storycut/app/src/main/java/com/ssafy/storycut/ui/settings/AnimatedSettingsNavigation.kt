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
    BackHandler(enabled = isVisible) {
        onDismiss()
    }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            SettingsScreen(
                authViewModel = authViewModel,
                onBackPressed = onDismiss,
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}
