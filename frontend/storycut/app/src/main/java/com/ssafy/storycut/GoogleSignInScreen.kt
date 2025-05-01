package com.ssafy.storycut

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch

private const val TAG = "GoogleSignIn"
private const val WEB_CLIENT_ID = "540631555497-buht2vm5kdkhv6er2o2t877mfn3sgi7b.apps.googleusercontent.com"

@Composable
fun GoogleSignInScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var googleIdToken by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                coroutineScope.launch {
                    signInWithGoogle(context, credentialManager) { token ->
                        googleIdToken = token
                    }
                }
            }
        ) {
            Text("구글로 로그인")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (googleIdToken != null) {
            Text("구글 ID 토큰:")
            Spacer(modifier = Modifier.height(8.dp))
            Text(googleIdToken!!)
        }
    }
}

private suspend fun signInWithGoogle(
    context: Context,
    credentialManager: CredentialManager,
    onTokenReceived: (String) -> Unit
) {
    try {
        // 구글 로그인 옵션 설정
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)
            .build()

        // 인증 요청 생성
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        // 사용자 인증 정보 가져오기
        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        handleSignInResult(result, onTokenReceived)
    } catch (e: GetCredentialException) {
        Log.e(TAG, "구글 로그인 실패: ${e.message}")

        // 승인된 계정이 없는 경우, 새 계정 선택 화면 표시
        if (e.message?.contains("사용자에게 계정이 없습니다") == true) {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                handleSignInResult(result, onTokenReceived)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "새 계정 선택 실패: ${e.message}")
            }
        }
    }
}

private fun handleSignInResult(
    result: GetCredentialResponse,
    onTokenReceived: (String) -> Unit
) {
    val credential = result.credential

    when (credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // 토큰을 콜백으로 전달
                    onTokenReceived(idToken)

                    // 실제 앱에서는 이 토큰을 서버에 전송하여 검증해야 합니다.
                    // 여기서는 단순히 화면에 표시하는 용도로만 사용합니다.
                    Log.d(TAG, "구글 ID 토큰: $idToken")
                } catch (e: GoogleIdTokenParsingException) {
                    Log.e(TAG, "올바르지 않은 구글 ID 토큰 응답 수신", e)
                }
            } else {
                Log.e(TAG, "예상치 못한 형식의 사용자 인증 정보")
            }
        }
        else -> {
            Log.e(TAG, "예상치 못한 형식의 사용자 인증 정보")
        }
    }
}