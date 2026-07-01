package com.example.examcall

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.examcall.model.ServerInfo
import com.example.examcall.network.ClientWebSocketManager
import com.example.examcall.network.UdpDiscovery
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ClientViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("examcall_client", Context.MODE_PRIVATE)

    private val _names = MutableStateFlow<List<String>>(emptyList())
    val names: StateFlow<List<String>> = _names

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _discoveredServers = MutableStateFlow<List<ServerInfo>>(emptyList())
    val discoveredServers: StateFlow<List<ServerInfo>> = _discoveredServers

    private val _connectedServer = MutableStateFlow<ServerInfo?>(null)
    val connectedServer: StateFlow<ServerInfo?> = _connectedServer

    private val _absentNames = MutableStateFlow<Set<String>>(emptySet())
    val absentNames: StateFlow<Set<String>> = _absentNames

    private var wsManager: ClientWebSocketManager? = null
    private var udpJob: Job? = null
    private var pruneJob: Job? = null

    init {
        startDiscovery()
    }

    fun startDiscovery() {
        udpJob?.cancel()
        pruneJob?.cancel()

        udpJob = UdpDiscovery.discoverServers(viewModelScope) { ip, name ->
            val now = System.currentTimeMillis()
            val current = _discoveredServers.value.toMutableList()
            val idx = current.indexOfFirst { it.ip == ip }
            if (idx >= 0) {
                current[idx] = current[idx].copy(lastSeen = now)
            } else {
                current.add(ServerInfo(ip, name, now))
            }
            _discoveredServers.value = current.toList()
        }

        pruneJob = viewModelScope.launch {
            while (isActive) {
                delay(5_000)
                val cutoff = System.currentTimeMillis() - 10_000
                _discoveredServers.value = _discoveredServers.value.filter { it.lastSeen > cutoff }
            }
        }
    }

    fun connectToServer(server: ServerInfo) {
        _connectedServer.value = server
        prefs.edit()
            .putString("lastServerIp", server.ip)
            .putString("lastServerName", server.name)
            .apply()

        wsManager?.disconnect()
        wsManager = ClientWebSocketManager(
            serverUrl = "ws://${server.ip}:8080",
            onNameList = { _names.value = it },
            onAbsentList = { _absentNames.value = it.toSet() },
            onConnectionChanged = { _isConnected.value = it }
        ).also { it.connect() }
    }

    fun disconnect() {
        wsManager?.disconnect()
        wsManager = null
        _connectedServer.value = null
        _isConnected.value = false
        _names.value = emptyList()
        _absentNames.value = emptySet()
    }

    fun sendCall(name: String) {
        wsManager?.send("""{"type":"call","name":"$name"}""")
    }

    override fun onCleared() {
        super.onCleared()
        udpJob?.cancel()
        pruneJob?.cancel()
        wsManager?.disconnect()
    }
}
