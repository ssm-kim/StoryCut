package com.ssafy.storycut.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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

    // 애니메이션 상태 관리
    // AnimatedVisibility 대신 직접 애니메이션 상태를 관리합니다
    var animationState by remember { mutableStateOf(false) }

    // isVisible이 변경될 때 애니메이션 상태 업데이트
    LaunchedEffect(isVisible) {
        animationState = isVisible
    }

    if (animationState || isVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(if (isVisible) 10f else 0f)
                .animateContentSize(animationSpec = tween(300))
                .offset(
                    x = androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (isVisible) 0.dp else 1000.dp,
                        animationSpec = tween(300)
                    ).value
                )
                .background(Color.White)
        ) {
            // 설정 화면 표시
            SettingsScreen(
                authViewModel = authViewModel,
                onBackPressed = onDismiss,
                onNavigateToLogin = onNavigateToLogin
            )
        }

        // 애니메이션이 완료된 후 상태 업데이트
        LaunchedEffect(isVisible) {
            kotlinx.coroutines.delay(300)
            if (!isVisible) {
                animationState = false
            }
        }
    }
}