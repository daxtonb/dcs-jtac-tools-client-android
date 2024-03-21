package com.daxtonb.dcsjtactoolsclient

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity() {

    private lateinit var client: OkHttpClient
    private var webSocket: WebSocket? = null
    private var udpSocket: DatagramSocket = DatagramSocket()
    private lateinit var webSocketStatusIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        client = OkHttpClient()

        val protocolSpinner: Spinner = findViewById(R.id.protocolSpinner)
        val serverAddressInput: EditText = findViewById(R.id.serverAddressInput)
        val portInput: EditText = findViewById(R.id.portInput)
        webSocketStatusIcon = findViewById(R.id.webSocketStatusIcon)
        val connectSwitch: Switch = findViewById(R.id.connectSwitch)

        // Set up the spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.protocol_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            protocolSpinner.adapter = adapter
        }

        serverAddressInput.setText("10.0.2.2")
        portInput.setText("9345")

        connectSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val protocol = protocolSpinner.selectedItem.toString()
                val serverAddress = serverAddressInput.text.toString()
                val port = portInput.text.toString()
                val fullAddress = "$protocol$serverAddress:$port"
                val request = Request.Builder().url(fullAddress).build()
                webSocket = client.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                        runOnUiThread { webSocketStatusIcon.setImageResource(R.drawable.baseline_power_24) }
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        runOnUiThread { webSocketStatusIcon.setImageResource(R.drawable.baseline_satellite_alt_24) }
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val localhostAddress: InetAddress = InetAddress.getByName("localhost")
                                val buf = text.toByteArray()
                                val packet = DatagramPacket(buf, buf.size, localhostAddress, 4242)
                                udpSocket.send(packet)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Handle any exceptions, possibly on the UI thread with withContext(Dispatchers.Main) { ... }
                            }
                        }
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        runOnUiThread { webSocketStatusIcon.setImageResource(R.drawable.baseline_power_off_24) }
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: okhttp3.Response?
                    ) {
                        runOnUiThread { webSocketStatusIcon.setImageResource(R.drawable.baseline_error_24) }
                    }
                })
            } else {
                webSocket?.close(1000, "User disconnected")
                webSocketStatusIcon.setImageResource(R.drawable.baseline_power_off_24)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.dispatcher.executorService.shutdown()
        udpSocket.close()
    }
}
