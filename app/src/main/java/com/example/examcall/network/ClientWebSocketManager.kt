package com.example.examcall.network

import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString

class ClientWebSocketManager(
    private val serverUrl: String,
    private val onNameList: (List<String>) -> Unit,
    private val onConnectionChanged: (Boolean) -> Unit,
) {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect() {
        val request = Request.Builder().url(serverUrl).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                onConnectionChanged(true)
                reconnectJob?.cancel()
            }

            override fun onMessage(ws: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {}

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                onConnectionChanged(false)
                scheduleReconnect()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                onConnectionChanged(false)
                scheduleReconnect()
            }
        })
    }

    private fun handleMessage(text: String) {
        try {
            val json = org.json.JSONObject(text)
            if (json.getString("type") == "name_list") {
                val arr = json.getJSONArray("names")
                val list = (0 until arr.length()).map { arr.getString(it) }
                onNameList(list)
            }
        } catch (_: Exception) {}
    }

    fun send(message: String) {
        webSocket?.send(message)
    }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(5_000)
            connect()
        }
    }

    fun disconnect() {
        reconnectJob?.cancel()
        scope.cancel()
        webSocket?.close(1000, null)
        client.dispatcher.executorService.shutdown()
    }
}
