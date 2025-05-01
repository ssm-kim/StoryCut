package com.ssafy.storycut

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private const val TAG = "GoogleSignIn"
private const val WEB_CLIENT_ID = "540631555497-buht2vm5kdkhv6er2o2t877mfn3sgi7b.apps.googleusercontent.com"

@Composable
fun GoogleSignInScreen2() {
    val context = LocalContext.current

    // 디버깅 정보 출력
    DisposableEffect(Unit) {
        printDebugInfo(context)
        onDispose { }
    }

    // Google 로그인 클라이언트 초기화
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // 로그인 상태 관리
    var idToken by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 기존 로그인 상태 확인
    DisposableEffect(Unit) {
        checkExistingSignIn(context) { token ->
            idToken = token
        }
        onDispose { }
    }

    // 로그인 결과 처리 런처
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Google 로그인 결과 수신: resultCode=${result.resultCode}")
        isLoading = false

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)

            idToken = account.idToken
            errorMessage = null
            Log.d(TAG, "로그인 성공, 이메일: ${account.email}, 이름: ${account.displayName}")
            Log.d(TAG, "ID 토큰: ${account.idToken}")
            Log.d(TAG, "계정 ID: ${account.id}")
        } catch (e: ApiException) {
            Log.e(TAG, "로그인 실패: statusCode=${e.statusCode}, 메시지=${e.message}", e)
            errorMessage = getErrorMessage(e.statusCode)
            idToken = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                Log.d(TAG, "로그인 버튼 클릭됨")
                performSignIn(googleSignInClient, signInLauncher) {
                    isLoading = true
                    errorMessage = null
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "로그인 중..." else "구글로 로그인")
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (idToken != null) {
            Text("구글 ID 토큰:")
            Spacer(modifier = Modifier.height(8.dp))
            Text(idToken!!)
        }
    }
}

private fun printDebugInfo(context: Context) {
    Log.d(TAG, "==== 디버그 정보 시작 ====")
    Log.d(TAG, "패키지 이름: ${context.packageName}")
    Log.d(TAG, "웹 클라이언트 ID: $WEB_CLIENT_ID")

    try {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_SIGNATURES
        )
        for (signature in info.signatures!!) {
            val md = MessageDigest.getInstance("SHA")
            md.update(signature.toByteArray())
            val shaBytes = md.digest()
            val sha1 = Base64.encodeToString(shaBytes, Base64.NO_WRAP)
            Log.d(TAG, "인증서 SHA-1 Base64: $sha1")

            // 일반적인 SHA-1 형식으로 출력 (16진수)
            val hexString = StringBuilder()
            for (b in shaBytes) {
                val hex = Integer.toHexString(0xFF and b.toInt())
                if (hex.length == 1) {
                    hexString.append('0')
                }
                hexString.append(hex)
            }
            Log.d(TAG, "인증서 SHA-1 Hex: $hexString")
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e(TAG, "패키지를 찾을 수 없습니다", e)
    } catch (e: NoSuchAlgorithmException) {
        Log.e(TAG, "SHA 알고리즘을 찾을 수 없습니다", e)
    }

    // Google Play 서비스 버전 확인
    try {
        val googlePlayServicesInfo = context.packageManager.getPackageInfo(
            "com.google.android.gms",
            0
        )
        Log.d(TAG, "Google Play 서비스 버전: ${googlePlayServicesInfo.versionName}")
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e(TAG, "Google Play 서비스를 찾을 수 없습니다", e)
    }

    // 기존 로그인 정보 확인
    val account = GoogleSignIn.getLastSignedInAccount(context)
    if (account != null) {
        Log.d(TAG, "기존 계정 정보:")
        Log.d(TAG, "- 이메일: ${account.email}")
        Log.d(TAG, "- 이름: ${account.displayName}")
        Log.d(TAG, "- ID: ${account.id}")
        Log.d(TAG, "- ID 토큰 존재: ${account.idToken != null}")
    } else {
        Log.d(TAG, "기존 로그인 계정 없음")
    }

    Log.d(TAG, "==== 디버그 정보 끝 ====")
}

private fun checkExistingSignIn(context: Context, onTokenFound: (String?) -> Unit) {
    val account = GoogleSignIn.getLastSignedInAccount(context)
    if (account != null && account.idToken != null) {
        Log.d(TAG, "기존 로그인 발견: ${account.email}")
        onTokenFound(account.idToken)
    }
}

private fun performSignIn(
    googleSignInClient: GoogleSignInClient,
    signInLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onSignInStart: () -> Unit
) {
    Log.d(TAG, "로그인 시작")
    onSignInStart()

    // 기존 로그인 정보 삭제
    googleSignInClient.signOut().addOnCompleteListener {
        Log.d(TAG, "기존 로그인 정보 삭제 완료")
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }
}

private fun getErrorMessage(statusCode: Int): String {
    return when (statusCode) {
        7 -> "네트워크 연결이 불안정합니다."
        8 -> "내부 오류가 발생했습니다."
        10 -> "개발자 오류: 앱이 올바르게 구성되지 않았습니다. (SHA-1 인증서 및 패키지 이름 확인 필요)"
        12500 -> "로그인이 취소되었습니다."
        12501 -> "로그인이 취소되었습니다."
        12502 -> "서버 오류가 발생했습니다."
        16 -> "네트워크 연결을 확인해주세요."
        else -> "로그인 실패: 코드 $statusCode"
    }
}