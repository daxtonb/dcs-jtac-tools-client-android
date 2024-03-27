package com.daxtonb.dcsjtactoolsclient

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class NetworkRepository {
    private var _client = OkHttpClient()
    private var _webSocket: WebSocket? = null
    private val _udpSocket: DatagramSocket = DatagramSocket()

    fun connectToHub(url: String, webSocketListener: WebSocketListener) {
        val request = Request.Builder().url(url).build()
        _webSocket = _client.newWebSocket(request, webSocketListener)
    }

    fun sendCursorOnTarget(text: String) {
        try {
            val localhostAddress: InetAddress = InetAddress.getByName("localhost")
            val buf = text.toByteArray()
            val packet = DatagramPacket(buf, buf.size, localhostAddress, 4242)
            _udpSocket.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun disconnectFromHub() {
        _webSocket?.close(1000, "Disconnect")
    }

    fun closeConnections() {
        _client.dispatcher.executorService.shutdown()
        _udpSocket.close()
        disconnectFromHub()
    }
}
