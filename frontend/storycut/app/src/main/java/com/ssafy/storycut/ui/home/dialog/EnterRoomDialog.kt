package com.ssafy.storycut.ui.home.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
@Composable
fun EnterRoomDialog(
    onDismiss: () -> Unit,
    onEnterRoom: (String, String?) -> Unit  // 초대코드와 비밀번호를 전달
) {
    var inviteCode by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }  // 비밀번호 추가
    var showErrorMessage by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.medium,
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "초대코드로 입장",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 초대코드 입력
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it },
                    label = { Text("초대코드") },
                    placeholder = { Text("초대코드를 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    isError = showErrorMessage && inviteCode.isBlank()
                )

                // 비밀번호 입력 (선택 사항)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("비밀번호 (선택사항)") },
                    placeholder = { Text("비밀번호가 있는 경우 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()  // 비밀번호 마스킹
                )

                // 에러 메시지
                if (showErrorMessage && inviteCode.isBlank()) {
                    Text(
                        text = "초대코드를 입력해주세요",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // 버튼 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "취소")
                    }

                    Button(
                        onClick = {
                            if (inviteCode.isBlank()) {
                                showErrorMessage = true
                            } else {
                                // 비밀번호가 비어있으면 null로 전달
                                val passwordToSend = if (password.isBlank()) null else password
                                onEnterRoom(inviteCode, passwordToSend)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "입장")
                    }
                }
            }
        }
    }
}