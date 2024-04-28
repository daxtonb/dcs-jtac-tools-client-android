package com.daxtonb.dcsjtactoolsclient.dcsjtachub.messages

abstract class HubMessageHandlerBase: HubMessageSpecification, HubMessageHandler {
    private val delimiter = "//"
    abstract fun getTopic(): String

    override fun isSatisfiedBy(message: String): Boolean {
        return message.startsWith("${getTopic()}$delimiter")
    }

    protected fun extractMessageBody(message: String): String {
        return message.substring(getTopic().length + delimiter.length)
    }
}