package com.daxtonb.dcsjtactoolsclient.dcsjtachub.messages

interface HubMessageSpecification {
    fun isSatisfiedBy(message: String): Boolean
}