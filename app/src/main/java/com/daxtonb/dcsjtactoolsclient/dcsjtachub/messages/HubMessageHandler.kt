package com.daxtonb.dcsjtactoolsclient.dcsjtachub.messages

interface HubMessageHandler {
    fun processMessage(message: String)
    fun dispose()
}