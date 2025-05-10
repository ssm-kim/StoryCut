package com.ssafy.storycut

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.ssafy.storycut.data.local.datastore.TokenManager
import com.ssafy.storycut.ui.navigation.AppNavigation
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

                    // 분리된 AppNavigation 컴포저블 사용
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