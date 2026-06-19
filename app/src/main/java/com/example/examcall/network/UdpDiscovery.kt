package com.example.examcall.network

import kotlinx.coroutines.*
import java.net.*

object UdpDiscovery {
    private const val PORT = 9090
    private const val PREFIX = "ExamCall:"

    fun startBroadcasting(ip: String, name: String, scope: CoroutineScope): Job {
        return scope.launch(Dispatchers.IO) {
            val broadcastAddr = getSubnetBroadcastAddress()
                ?: InetAddress.getByName("255.255.255.255")
            DatagramSocket().use { socket ->
                socket.broadcast = true
                val message = "$PREFIX$ip:$name".toByteArray(Charsets.UTF_8)
                while (isActive) {
                    try {
                        val packet = DatagramPacket(message, message.size, broadcastAddr, PORT)
                        socket.send(packet)
                    } catch (_: Exception) {}
                    delay(3000)
                }
            }
        }
    }

    fun discoverServers(scope: CoroutineScope, onUpdate: (ip: String, name: String) -> Unit): Job {
        return scope.launch(Dispatchers.IO) {
            try {
                DatagramSocket(PORT).use { socket ->
                    socket.soTimeout = 1000
                    val buf = ByteArray(256)
                    while (isActive) {
                        try {
                            val packet = DatagramPacket(buf, buf.size)
                            socket.receive(packet)
                            val text = String(packet.data, 0, packet.length, Charsets.UTF_8)
                            if (text.startsWith(PREFIX)) {
                                val payload = text.removePrefix(PREFIX)
                                val colonIdx = payload.indexOf(':')
                                if (colonIdx > 0) {
                                    onUpdate(
                                        payload.substring(0, colonIdx),
                                        payload.substring(colonIdx + 1)
                                    )
                                }
                            }
                        } catch (_: SocketTimeoutException) {}
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun getDeviceIpAddress(): String? {
        return try {
            NetworkInterface.getNetworkInterfaces()
                ?.asSequence()
                ?.filter { !it.isLoopback && it.isUp }
                ?.flatMap { it.interfaceAddresses.asSequence() }
                ?.firstOrNull { it.address is Inet4Address && !it.address.isLoopbackAddress }
                ?.address?.hostAddress
        } catch (_: Exception) { null }
    }

    private fun getSubnetBroadcastAddress(): InetAddress? {
        return try {
            NetworkInterface.getNetworkInterfaces()
                ?.asSequence()
                ?.filter { !it.isLoopback && it.isUp }
                ?.flatMap { it.interfaceAddresses.asSequence() }
                ?.firstOrNull { it.address is Inet4Address && !it.address.isLoopbackAddress }
                ?.broadcast
        } catch (_: Exception) { null }
    }
}
