package com.example.examcall.network

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.Collections

class EmbeddedWebSocketServer(
    private val onCallReceived: (name: String) -> Unit,
    private val onClientCountChanged: (Int) -> Unit,
) : WebSocketServer(InetSocketAddress(8080)) {

    private val clients = Collections.synchronizedSet(HashSet<WebSocket>())
    private var currentNames: List<String> = emptyList()

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        clients.add(conn)
        onClientCountChanged(clients.size)
        conn.send(buildNameListJson(currentNames))
    }

    override fun onMessage(conn: WebSocket, message: String) {
        try {
            val json = org.json.JSONObject(message)
            if (json.getString("type") == "call") {
                onCallReceived(json.getString("name"))
            }
        } catch (_: Exception) {}
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        clients.remove(conn)
        onClientCountChanged(clients.size)
    }

    override fun onError(conn: WebSocket?, ex: Exception) {}

    override fun onStart() {}

    fun broadcastNameList(names: List<String>) {
        currentNames = names
        val json = buildNameListJson(names)
        synchronized(clients) {
            clients.forEach { if (it.isOpen) it.send(json) }
        }
    }

    private fun buildNameListJson(names: List<String>): String {
        val namesJson = names.joinToString(",") { "\"$it\"" }
        return """{"type":"name_list","names":[$namesJson]}"""
    }
}
