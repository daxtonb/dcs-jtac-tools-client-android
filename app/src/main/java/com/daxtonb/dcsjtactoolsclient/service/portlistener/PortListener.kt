package com.daxtonb.dcsjtactoolsclient.service.portlistener

import android.util.Log
import java.io.IOException
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException

class PortListener(
    private val port: Int,
    private val dataReceivedHandler: (String) -> Unit
) : Runnable {
    private var _isListening = true
    private var _socket: DatagramSocket? = null
    private val _expectedEndOfData = "}}"

    internal val isListening: Boolean get() = _isListening

    override fun run() {
        val buffer = ScalingBuffer(_expectedEndOfData)
        while (_isListening) {
            val data = read(buffer)
            dataReceivedHandler(data)
        }
    }

    fun stop() {
        _isListening = false
        closeConnection()
    }

    internal fun closeConnection() {
        _socket?.close()
    }

    internal fun read(buffer: ScalingBuffer): String {
        try {
            val address = InetSocketAddress(port)
            _socket = DatagramSocket(address)
            return buffer.readNext(_socket!!)
        } catch (ex: SocketException) {
            Log.d("NetworkListener", "Quietly handling exception", ex)
        } catch (ex: IOException) {
            Log.e("NetworkListener", "Error reading port  ${port}:", ex)
        }  catch (ex: Exception) {
            Log.e("NetworkListener", "Unexpected error:", ex)
        } finally {
            closeConnection()
        }

        return ""
    }
}