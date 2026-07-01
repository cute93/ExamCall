package com.example.examcall

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.examcall.ui.*
import com.example.examcall.ui.theme.ExamCallTheme

class MainActivity : ComponentActivity() {
    private val serverViewModel: ServerViewModel by viewModels()
    private val clientViewModel: ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            ExamCallTheme {
                var screen by remember { mutableStateOf<Screen>(Screen.ModeSelect) }

                when (screen) {
                    Screen.ModeSelect -> ModeSelectScreen(
                        onSelectServer = { name ->
                            serverViewModel.startServer(name)
                            screen = Screen.Server
                        },
                        onSelectClient = { screen = Screen.ServerList },
                        onExit = { finish() }
                    )
                    Screen.Server -> {
                        val names by serverViewModel.names.collectAsState()
                        val calledName by serverViewModel.calledName.collectAsState()
                        val clientCount by serverViewModel.clientCount.collectAsState()
                        val serverName by serverViewModel.serverName.collectAsState()
                        val absentNames by serverViewModel.absentNames.collectAsState()
                        ServerScreen(
                            serverName = serverName,
                            names = names,
                            calledName = calledName,
                            clientCount = clientCount,
                            absentNames = absentNames,
                            onAddName = serverViewModel::addName,
                            onDeleteName = serverViewModel::deleteName,
                            onToggleAbsent = serverViewModel::toggleAbsent,
                            onDismiss = serverViewModel::dismissCalledName,
                            onExit = { screen = Screen.ModeSelect }
                        )
                    }
                    Screen.ServerList -> {
                        val servers by clientViewModel.discoveredServers.collectAsState()
                        ServerListScreen(
                            servers = servers,
                            onSelect = { server ->
                                clientViewModel.connectToServer(server)
                                screen = Screen.Client
                            },
                            onExit = { screen = Screen.ModeSelect }
                        )
                    }
                    Screen.Client -> {
                        val names by clientViewModel.names.collectAsState()
                        val isConnected by clientViewModel.isConnected.collectAsState()
                        val connectedServer by clientViewModel.connectedServer.collectAsState()
                        val absentNames by clientViewModel.absentNames.collectAsState()
                        ClientScreen(
                            serverName = connectedServer?.name ?: "",
                            names = names,
                            isConnected = isConnected,
                            absentNames = absentNames,
                            onCall = clientViewModel::sendCall,
                            onChangeServer = {
                                clientViewModel.disconnect()
                                screen = Screen.ServerList
                            },
                            onExit = { screen = Screen.ModeSelect }
                        )
                    }
                }
            }
        }
    }
}

sealed class Screen {
    object ModeSelect : Screen()
    object Server : Screen()
    object ServerList : Screen()
    object Client : Screen()
}
