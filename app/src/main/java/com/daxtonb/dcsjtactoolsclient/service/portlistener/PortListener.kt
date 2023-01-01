package com.daxtonb.dcsjtactoolsclient.service.portlistener

import android.util.Log
import java.io.IOException
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException

class PortListener(
    private val buffer: ScalingBuffer,
    private val port: Int) {
    private val _socket: DatagramSocket
    val isPortClosed get(): Boolean = _socket.isClosed

    init {
        val address = InetSocketAddress(port)
        _socket = DatagramSocket(address)
    }

    fun read(): String {
        try {
            return buffer.readNext(_socket)
        } catch (ex: SocketException) {
            Log.d("PortListener", "Quietly handling exception", ex)
        } catch (ex: IOException) {
            Log.e("PortListener", "Error reading port  $port:", ex)
        }  catch (ex: Exception) {
            Log.e("PortListener", "Unexpected error:", ex)
        } finally {
            closePort()
        }

        return ""
    }

    fun closePort() {
        _socket.close()
    }
}