package com.example.examcall.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModeSelectScreen(
    onSelectServer: (name: String) -> Unit,
    onSelectClient: () -> Unit,
    onExit: () -> Unit
) {
    var showNameDialog by remember { mutableStateOf(false) }
    var serverName by remember { mutableStateOf("") }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("서버 이름 설정") },
            text = {
                OutlinedTextField(
                    value = serverName,
                    onValueChange = { serverName = it },
                    label = { Text("이름 (예: 1반, 시험장A)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (serverName.isNotBlank()) {
                        showNameDialog = false
                        onSelectServer(serverName.trim())
                    }
                }) { Text("시작") }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) { Text("취소") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onExit,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "종료")
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ExamCall", fontSize = 40.sp, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { showNameDialog = true },
                modifier = Modifier.fillMaxWidth().height(72.dp)
            ) {
                Text("서버 (명단 관리)", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSelectClient,
                modifier = Modifier.fillMaxWidth().height(72.dp)
            ) {
                Text("클라이언트 (호출 버튼)", fontSize = 22.sp)
            }
        }
    }
}
