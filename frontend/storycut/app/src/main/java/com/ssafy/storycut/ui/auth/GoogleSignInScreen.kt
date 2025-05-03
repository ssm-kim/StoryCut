package com.ssafy.storycut.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GoogleSignInScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val credentialManager = remember { CredentialManager.create(context) }
    val scrollState = rememberScrollState()

    val uiState = viewModel.uiState.collectAsState().value
    val tokens = viewModel.tokenState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Button(
            onClick = {
                viewModel.signInWithGoogle(context, credentialManager)
            },
            enabled = uiState !is AuthUiState.Loading,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("구글로 로그인")
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (uiState) {
            is AuthUiState.Loading -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }
            is AuthUiState.Success -> {
                Text(
                    text = "로그인 상태: ${uiState.message}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            is AuthUiState.Error -> {
                Text(
                    text = "오류: ${uiState.message}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> { /* 초기 상태에는 아무것도 표시하지 않음 */ }
        }

        tokens?.let { (accessToken, refreshToken) ->
            Spacer(modifier = Modifier.height(16.dp))
            ResponseSection(title = "액세스 토큰", content = accessToken)

            Spacer(modifier = Modifier.height(16.dp))
            ResponseSection(title = "리프레시 토큰", content = refreshToken)
        }
    }
}

@Composable
private fun ResponseSection(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider() // Divider에서 HorizontalDivider로 변경
    }
}