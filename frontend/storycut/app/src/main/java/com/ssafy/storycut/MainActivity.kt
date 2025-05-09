package com.ssafy.storycut

import ShortsScreen
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.auth.AuthViewModel
import com.ssafy.storycut.ui.auth.LoginScreen
import com.ssafy.storycut.ui.main.MainScreen
import com.ssafy.storycut.ui.splash.SplashScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    // 딥링크 정보를 저장할 변수
    private val deepLinkTokenState = mutableStateOf<String?>(null)
    private val isDeepLinkState = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기 인텐트 처리
        handleIntent(intent)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val deepLinkToken = remember { deepLinkTokenState }
                    val isDeepLink = remember { isDeepLinkState }

                    AppNavigation(
                        tokenManager = tokenManager,
                        deepLinkToken = deepLinkToken.value,
                        isDeepLink = isDeepLink.value
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)  // 새 인텐트 설정
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.let { uri ->
            val token = uri.getQueryParameter("token")
            if (!token.isNullOrEmpty()) {
                Log.d(TAG, "딥링크 URI: $uri, 토큰: $token")
                deepLinkTokenState.value = token
                isDeepLinkState.value = true
            }
        }
    }
}



@Composable
fun AppNavigation(
    tokenManager: TokenManager,
    deepLinkToken: String?,
    isDeepLink: Boolean
) {
    val navController = rememberNavController()
    val authViewModel = hiltViewModel<AuthViewModel>()

    // 시작 화면 결정 - 딥링크로 들어온 경우 스플래시 화면 건너뛰기
    val startDestination = if (isDeepLink) "shorts" else "splash"

    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val navigateToLogin = activity?.intent?.getBooleanExtra("NAVIGATE_TO_LOGIN", false) ?: false

    LaunchedEffect(deepLinkToken) {
    // 딥링크 토큰 처리
        if (!deepLinkToken.isNullOrEmpty()) {
            Log.d(TAG, "딥링크 토큰 저장: $deepLinkToken")
            tokenManager.saveGoogleAccessTokens(deepLinkToken)
        }
    }

    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin) {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // NavHost 설정
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("splash") {
            SplashScreen(navController, authViewModel)
        }
        composable("login") {
            LoginScreen(navController, authViewModel)
        }
        composable("main") {
            MainScreen(
                authViewModel = authViewModel,
                tokenManager = tokenManager
            )
        }
        composable("shorts") {
            ShortsScreen()
        }
    }
}