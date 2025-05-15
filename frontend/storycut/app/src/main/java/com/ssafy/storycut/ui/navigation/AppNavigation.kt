// AppNavigation.kt
package com.ssafy.storycut.ui.navigation

import ShortsScreen
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.auth.AuthViewModel
import com.ssafy.storycut.ui.auth.LoginScreen
import com.ssafy.storycut.ui.mypage.VideoDetailScreen
import com.ssafy.storycut.ui.splash.SplashScreen

private const val TAG = "AppNavigation"

@Composable
fun AppNavigation(
    tokenManager: TokenManager,
    deepLinkToken: String? = null,
    isDeepLink: Boolean = false,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    val startDestination = if (isDeepLink) Navigation.SHORTS else Navigation.SPLASH

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val navigateToLogin = activity?.intent?.getBooleanExtra("NAVIGATE_TO_LOGIN", false) ?: false

    // 딥링크 토큰 처리
    LaunchedEffect(deepLinkToken) {
        if (!deepLinkToken.isNullOrEmpty()) {
            Log.d(TAG, "딥링크 토큰 저장: $deepLinkToken")
            tokenManager.saveGoogleAccessTokens(deepLinkToken)
        }
    }

    // 로그인 화면으로 즉시 이동해야 하는 경우
    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin) {
            navController.navigate(Navigation.LOGIN) {
                popUpTo(Navigation.SPLASH) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 스플래시 화면
        composable(Navigation.SPLASH) {
            SplashScreen(navController, authViewModel)
        }

        // 로그인 화면
        composable(Navigation.LOGIN) {
            LoginScreen(navController, authViewModel)
        }

        // 메인 화면 (하단 탭 네비게이션 포함)
        composable(Navigation.MAIN) {
            MainScreen(
                authViewModel = authViewModel,
                tokenManager = tokenManager,
                onNavigateToLogin = {
                    // 로그아웃 후 로그인 화면으로 이동 - 최상위 네비게이션 컨트롤러 사용
                    navController.navigate(Navigation.LOGIN) {
                        popUpTo(0) { inclusive = true }  // 모든 백스택 제거
                    }
                }
            )
        }

        // 최상위 쇼츠 화면 (딥링크로 접근 가능)
        composable(Navigation.SHORTS) {
            ShortsScreen()
        }

    }
}