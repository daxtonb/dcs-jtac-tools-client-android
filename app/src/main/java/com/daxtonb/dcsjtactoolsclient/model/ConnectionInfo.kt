package com.daxtonb.dcsjtactoolsclient.model

class ConnectionInfo (
    var address: String? = "",
    var port: String? = "4300",
    var isListening: Boolean = false,
    var lastReceivedData: String? = null
)