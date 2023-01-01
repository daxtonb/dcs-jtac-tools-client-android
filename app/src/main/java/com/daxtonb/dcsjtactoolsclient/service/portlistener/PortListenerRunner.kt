package com.daxtonb.dcsjtactoolsclient.service.portlistener

class PortListenerRunner(
    private val portListener: PortListener,
    private val dataReceivedHandler: (String) -> Unit
) : Runnable {
    private var _isListening = true
    val isListening = _isListening

    override fun run() {
        _isListening = true
        while (_isListening) {
            val data = portListener.read()
            dataReceivedHandler(data)
        }
    }

    fun stop() {
        _isListening = false
        portListener.closePort()
    }
}