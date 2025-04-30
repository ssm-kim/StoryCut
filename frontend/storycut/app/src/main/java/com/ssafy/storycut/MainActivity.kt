package com.ssafy.storycut

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GoogleSignInScreen()
                }
            }
        }
    }
}

@Composable
fun GoogleSignInScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userInfo by remember { mutableStateOf<UserEntity?>(null) }
    var authCode by remember { mutableStateOf<String?>(null) }

    // 데이터베이스 및 리포지토리 설정
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { UserRepository(database.userDao()) }

    // 이전에 로그인한 사용자 확인
    LaunchedEffect(Unit) {
        userInfo = repository.getLastLoggedInUser()
        authCode = userInfo?.authCode
    }

    // 구글 로그인 클라이언트 설정 - 인증 코드를 요청하도록 수정
    val serverClientId = "ZZ" // 실제 서버 클라이언트 ID로 교체
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestServerAuthCode(serverClientId) // 인증 코드 요청 추가
        .requestScopes(Scope("openid"), Scope("profile")) // 필요한 스코프 추가
        .build()

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // 로그인 결과 처리를 위한 런처
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            // 인증 코드 가져오기
            account?.let { googleAccount ->
                // 인증 코드 상태 업데이트
                authCode = googleAccount.serverAuthCode

                val user = UserEntity(
                    email = googleAccount.email ?: "",
                    displayName = googleAccount.displayName,
                    photoUrl = googleAccount.photoUrl?.toString(),
                    authCode = googleAccount.serverAuthCode, // 인증 코드 저장
                    lastLoginTime = System.currentTimeMillis() // 현재 시간으로 로그인 시간 설정
                )

                scope.launch {
                    repository.saveUser(user)
                    userInfo = user
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "로그인 실패: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (userInfo == null) {
            Button(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                }
            ) {
                Text("구글로 로그인")
            }
        } else {
            Text("로그인 성공!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("이메일: ${userInfo?.email}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("이름: ${userInfo?.displayName ?: "이름 없음"}")

            // 인증 코드 표시
            Spacer(modifier = Modifier.height(16.dp))
            Text("인증 코드:", style = MaterialTheme.typography.titleMedium)
            Text(authCode ?: "인증 코드 없음",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        userInfo = null
                        authCode = null
                    }
                }
            ) {
                Text("로그아웃")
            }
        }
    }
}