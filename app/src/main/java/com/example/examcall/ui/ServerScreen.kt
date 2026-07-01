package com.example.examcall.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ServerScreen(
    serverName: String,
    names: List<String>,
    calledName: String?,
    clientCount: Int,
    absentNames: Set<String>,
    onAddName: (String) -> Unit,
    onDeleteName: (String) -> Unit,
    onToggleAbsent: (String) -> Unit,
    onDismiss: () -> Unit,
    onExit: () -> Unit
) {
    var newName by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                serverName.ifBlank { "서버" },
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("클라이언트: ${clientCount}명", fontSize = 16.sp)
                IconButton(onClick = onExit) {
                    Icon(Icons.Default.Close, contentDescription = "종료")
                }
            }
        }

        if (calledName != null) {
            CalledNameCard(name = calledName, onDismiss = onDismiss)
            Spacer(modifier = Modifier.height(16.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text("명단 관리", fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("이름") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (newName.isNotBlank()) {
                    onAddName(newName.trim())
                    newName = ""
                }
            }) {
                Text("추가")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(names) { name ->
                val isAbsent = name in absentNames
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        name,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                        color = if (isAbsent) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurface
                    )
                    FilledTonalButton(
                        onClick = { onToggleAbsent(name) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isAbsent) MaterialTheme.colorScheme.errorContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isAbsent) MaterialTheme.colorScheme.error
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(if (isAbsent) "부재중" else "자리있음", fontSize = 13.sp)
                    }
                    IconButton(onClick = { onDeleteName(name) }) {
                        Icon(Icons.Default.Delete, contentDescription = "삭제")
                    }
                }
            }
        }
    }
}

@Composable
private fun CalledNameCard(name: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 64.sp,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineLarge
            )
            Button(onClick = onDismiss, modifier = Modifier.height(64.dp)) {
                Text("확인", fontSize = 20.sp)
            }
        }
    }
}
