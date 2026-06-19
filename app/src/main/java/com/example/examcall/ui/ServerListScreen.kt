package com.example.examcall.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.examcall.model.ServerInfo

@Composable
fun ServerListScreen(
    servers: List<ServerInfo>,
    onSelect: (ServerInfo) -> Unit,
    onExit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("서버 선택", fontSize = 24.sp, style = MaterialTheme.typography.headlineMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("● 탐색 중...", color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "종료")
                }
            }
        }

        if (servers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "서버 탐색 중...\n서버 태블릿의 앱을 먼저 실행해주세요.",
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(servers) { server ->
                    Card(
                        onClick = { onSelect(server) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(server.name, fontSize = 22.sp, modifier = Modifier.weight(1f))
                            Text(server.ip, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}
