package com.ssafy.storycut.ui.home.dialog


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ssafy.storycut.R
import com.ssafy.storycut.data.api.model.room.CreateRoomRequest

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreateRoom: (CreateRoomRequest) -> Unit
) {
    var roomTitle by remember { mutableStateOf("") }
    var roomPassword by remember { mutableStateOf("") }
    var roomContext by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
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
                    text = "공유방 생성",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 방 제목 입력
                OutlinedTextField(
                    value = roomTitle,
                    onValueChange = { roomTitle = it },
                    label = { Text("방 제목") },
                    placeholder = { Text("방 제목을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true,
                    isError = showErrorMessage && roomTitle.isBlank()
                )

                // 방 비밀번호 입력
                OutlinedTextField(
                    value = roomPassword,
                    onValueChange = { roomPassword = it },
                    label = { Text("비밀번호 (선택)") },
                    placeholder = { Text("비밀번호를 입력하세요") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible
                        }) {
                            Icon(
                                painter = painterResource(
                                    id = if (passwordVisible)
                                        R.drawable.visibility_on
                                    else
                                        R.drawable.visibility_off
                                ),
                                contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기"
                                ,            modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    singleLine = true
                )

                // 방 설명 입력
                OutlinedTextField(
                    value = roomContext,
                    onValueChange = { roomContext = it },
                    label = { Text("방 설명") },
                    placeholder = { Text("방 설명을 입력하세요") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(vertical = 8.dp),
                    isError = showErrorMessage && roomContext.isBlank()
                )

                // 에러 메시지
                if (showErrorMessage && (roomTitle.isBlank() || roomContext.isBlank())) {
                    Text(
                        text = "방 제목과 설명은 필수 입력 항목입니다",
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
                            if (roomTitle.isBlank() || roomContext.isBlank()) {
                                showErrorMessage = true
                            } else {
                                // CreateRoomRequest 객체 생성
                                val request = CreateRoomRequest(
                                    roomTitle = roomTitle,
                                    roomPassword = if (roomPassword.isBlank()) null else roomPassword,
                                    roomContext = roomContext
                                )
                                onCreateRoom(request)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "생성")
                    }
                }
            }
        }
    }
}