package com.ssafy.storycut.ui.splash

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.ssafy.storycut.ui.auth.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "SplashScreen"

@Composable
fun SplashScreen(navController: NavHostController, authViewModel: AuthViewModel) {
    val userInfo by authViewModel.userState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // 스플래시 화면이 보이는 동안 토큰 유효성 확인 시도
            Log.d(TAG, "토큰 유효성 확인 시작...")
            authViewModel.checkTokenValidity()
            
            // 최소 1.5초 동안 스플래시 화면 보여주기
            delay(1500) 
            
            Log.d(TAG, "사용자 정보 상태: ${userInfo != null}")
            
            // 사용자 정보 여부에 따라 적절한 화면으로 이동
            if (userInfo != null) {
                Log.d(TAG, "사용자 정보 확인 완료, 메인 화면으로 이동")
                navController.navigate("main") {
                    popUpTo("splash") { inclusive = true }
                }
            } else {
                Log.d(TAG, "사용자 정보 없음, 로그인 화면으로 이동")
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // 스플래시 화면 UI
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
        // 로고 이미지를 추가할 수 있음
        // Image(painter = painterResource(id = R.drawable.splash_logo), contentDescription = "Logo")
    }
}