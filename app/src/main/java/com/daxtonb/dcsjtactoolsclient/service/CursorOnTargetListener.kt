package com.daxtonb.dcsjtactoolsclient.service

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket

class CursorOnTargetListener(request: Request) {

    private lateinit var client: OkHttpClient
    private var webSocket: WebSocket? = null

    init {

    }
}