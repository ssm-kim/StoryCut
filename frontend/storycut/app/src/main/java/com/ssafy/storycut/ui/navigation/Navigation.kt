package com.ssafy.storycut.ui.navigation

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions

object Navigation {
    // 앱 최상위 경로
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
    const val SHORTS = "shorts"

    // 메인 화면 내부 경로
    object Main {
        const val HOME = "home"
        const val EDIT = "edit"
        const val SHORTS_UPLOAD = "shorts_upload"
        const val MYPAGE = "mypage"
        const val SETTINGS = "settings"
    }
}

/**
 * 네비게이션 관련 확장 함수들
 */

// 로그인 화면으로 이동
fun NavController.navigateToLogin() {
    Log.d("Navigation", "로그인 화면으로 이동 시작")
    navigate(Navigation.LOGIN) {
        popUpTo(Navigation.MAIN) { inclusive = true }
        launchSingleTop = true
    }
}
// 메인 화면으로 이동
fun NavController.navigateToMain() {
    navigate(Navigation.MAIN) {
        popUpTo(Navigation.SPLASH) { inclusive = true }
    }
}

// 스플래시 화면으로 이동
fun NavController.navigateToSplash() {
    navigate(Navigation.SPLASH)
}

// 쇼츠 화면으로 이동
fun NavController.navigateToShorts() {
    navigate(Navigation.SHORTS)
}

// 메인 화면 내에서 탭 이동 (하단 탭 네비게이션)
fun NavController.navigateToMainTab(route: String) {
    navigate(route) {
        // 네비게이션 컨트롤러의 스택을 초기화하여 백 스택 관리
        popUpTo(this@navigateToMainTab.graph.findStartDestination().id) {
            saveState = true
        }
        // 같은 아이템을 여러 번 클릭했을 때 같은 화면의 여러 인스턴스가 쌓이는 것을 방지
        launchSingleTop = true
        // 이전 상태를 복원
        restoreState = true
    }
}
