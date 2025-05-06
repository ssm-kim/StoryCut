package com.ssafy.storycut

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ssafy.storycut.ui.auth.AuthUiState
import com.ssafy.storycut.ui.auth.AuthViewModel
import com.ssafy.storycut.ui.auth.GoogleSignInScreen
import com.ssafy.storycut.ui.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // 여기서 AuthViewModel을 생성하여 앱 전체에서 공유
    val authViewModel = hiltViewModel<AuthViewModel>()
    
    // 사용자 인증 상태 관찰
    val userInfo = authViewModel.userState.collectAsState().value
    
    // NavHost 설정
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            // 이미 로그인한 사용자는 메인 화면으로 자동 이동
            LaunchedEffect(userInfo) {
                if (userInfo != null) {
                    Log.d(TAG, "사용자 정보 발견: $userInfo, 메인 화면으로 이동")
                    authViewModel.setLoginSuccess()
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
            
            // 로그인 화면 표시
            LoginScreen(navController, authViewModel)
        }
        
        composable("main") {
            // 인증 체크: 로그인하지 않은 사용자는 로그인 화면으로 강제 이동
            LaunchedEffect(userInfo) {
                if (userInfo == null) {
                    Log.d(TAG, "인증되지 않은 사용자가 메인 화면에 접근 시도, 로그인 화면으로 리디렉션")
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                } else {
                    Log.d(TAG, "메인 화면에서 사용자 정보: $userInfo")
                }
            }
            
            // 메인 화면에 AuthViewModel 전달
            MainScreen(authViewModel = authViewModel)
        }
    }
}

@Composable
fun LoginScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    GoogleSignInScreen(viewModel = authViewModel)
    
    // 인증 상태 관찰
    val authState by authViewModel.uiState.collectAsState()
    
    // LaunchedEffect를 사용하여 authState 값이 변경될 때마다 체크
    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Success -> {
                Log.d(TAG, "로그인 성공, 메인 화면으로 이동")
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }
            is AuthUiState.Error -> {
                // 오류 발생 시 로그 출력
                val errorMsg = (authState as AuthUiState.Error).message
                Log.e(TAG, "로그인 오류: $errorMsg")
            }
            else -> {
                // 다른 상태는 무시
            }
        }
    }
}