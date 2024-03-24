import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class NetworkRepository(private val client: OkHttpClient) {
    private var webSocket: WebSocket? = null
    private val udpSocket: DatagramSocket = DatagramSocket()

    fun connectToHub(url: String, webSocketListener: WebSocketListener) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, webSocketListener)
    }

    fun sendCursorOnTarget(text: String) {
        try {
            val localhostAddress: InetAddress = InetAddress.getByName("localhost")
            val buf = text.toByteArray()
            val packet = DatagramPacket(buf, buf.size, localhostAddress, 4242)
            udpSocket.send(packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun closeConnections() {
        client.dispatcher.executorService.shutdown()
        udpSocket.close()
        webSocket?.close(1000, "Disconnect")
    }
}
