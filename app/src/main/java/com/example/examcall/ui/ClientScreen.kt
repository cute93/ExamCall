package com.example.examcall.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClientScreen(
    serverName: String,
    names: List<String>,
    isConnected: Boolean,
    onCall: (String) -> Unit,
    onChangeServer: () -> Unit,
    onExit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(serverName, fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isConnected) "● 연결됨" else "● 연결 중...",
                    color = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onChangeServer) { Text("서버 변경") }
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "종료")
                }
            }
        }

        if (names.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "명단이 없습니다.\n서버 태블릿에서 명단을 추가해주세요.",
                    fontSize = 16.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(names) { name ->
                    Button(
                        onClick = { onCall(name) },
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                    ) {
                        Text(name, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}
