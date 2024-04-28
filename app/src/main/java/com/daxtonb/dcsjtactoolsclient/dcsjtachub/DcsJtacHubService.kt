package com.daxtonb.dcsjtactoolsclient

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.daxtonb.dcsjtactoolsclient.dcsjtachub.messages.HubMessageHandler
import com.daxtonb.dcsjtactoolsclient.dcsjtachub.messages.HubMessageSpecification
import com.daxtonb.dcsjtactoolsclient.dcsjtachub.messages.UnitsMessageHandler
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class DcsJtacHubService : Service() {

    private val _unitsTopic = "UNITS"
    private val _binder = LocalBinder()
    private val _unitNames = MutableLiveData<List<String>>()
    private val _unitNamesSet = mutableSetOf<String>()
    private var _selectedUnitName = MutableLiveData<String?>()
    private lateinit var _messageHandlers: List<HubMessageHandler>
    private lateinit var _networkRepository: NetworkRepository

    val webSocketStatus = MutableLiveData(R.drawable.baseline_power_off_24)
    val unitNames: LiveData<List<String>> = _unitNames

    override fun onCreate() {
        super.onCreate()
        _networkRepository = NetworkRepository()
        _messageHandlers = listOf(
            UnitsMessageHandler(
                _networkRepository,
                _selectedUnitName,
                _unitNamesSet,
                _unitNames,
                this
            )
        )
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Create a notification channel & start the service in the foreground
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DCS JTAC Tools")
            .setContentText("Actively listening for data from DCS JTAC Hub...")
            .setSmallIcon(R.drawable.baseline_satellite_alt_24)
            //.setContentIntent(pendingIntent) // Optional, if you have a UI to launch
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY // Or START_STICKY/START_REDELIVER_INTENT based on your needs
    }

    override fun onBind(intent: Intent): IBinder {
        return _binder
    }

    override fun onDestroy() {
        super.onDestroy()
        _networkRepository.closeConnections()
        _messageHandlers.forEach {
            it.dispose()
        }
    }

    fun connectToHub(url: String) {
        _networkRepository.connectToHub(url, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                _networkRepository.subscribeTopic(_unitsTopic)
                webSocketStatus.postValue(R.drawable.baseline_power_24)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                webSocketStatus.postValue(R.drawable.baseline_satellite_alt_24)
                _messageHandlers.forEach {
                    if (it is HubMessageSpecification && it.isSatisfiedBy(text)) {
                        it.processMessage(text)
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocketStatus.postValue(R.drawable.baseline_power_off_24)
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: okhttp3.Response?
            ) {
                webSocketStatus.postValue(R.drawable.baseline_error_24)
            }
        })
    }

    fun setSelectedUnit(unitName: String) {
        synchronized(_selectedUnitName) {
            _selectedUnitName.value = unitName
        }
    }

    fun disconnectFromHub() {
        _networkRepository.disconnectFromHub()
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    companion object {
        const val CHANNEL_ID = "DcsJtacHubChannel"
        const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): DcsJtacHubService = this@DcsJtacHubService
    }

}
