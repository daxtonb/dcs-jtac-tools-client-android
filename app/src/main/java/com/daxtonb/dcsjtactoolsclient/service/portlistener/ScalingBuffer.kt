package com.daxtonb.dcsjtactoolsclient.service.portlistener

import java.net.DatagramPacket
import java.net.DatagramSocket

class ScalingBuffer(
    private val expectedEndOfData: String
) {
    internal val defaultBufferSize: Int = 128
    private var _bufferSize: Int = defaultBufferSize
    internal val currentBufferSize: Int get() = _bufferSize

    fun readNext(socket: DatagramSocket): String {
        val buffer = ByteArray(_bufferSize)
        val packet = DatagramPacket(buffer, buffer.size)
        socket.receive(packet)
        val stringData = deserializeData(packet)
        scaleBufferToDataSize(stringData)
        return buildReturnValue(stringData)
    }

    internal fun deserializeData(packet: DatagramPacket): String {
        return String(packet.data, 0, packet.length)
    }

    internal fun allDataIsInBuffer(data: String): Boolean {
        return  data.endsWith((expectedEndOfData))
    }

    internal fun scaleBufferToDataSize(data: String) {
        if (data.isEmpty())
            return
        if (allDataIsInBuffer(data))
            _bufferSize = data.length
        else
            _bufferSize *= 2
    }

    internal fun buildReturnValue(data: String): String {
        return if (allDataIsInBuffer(data)) data else ""
    }
}