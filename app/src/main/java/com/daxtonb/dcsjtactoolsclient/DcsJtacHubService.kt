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
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.simpleframework.xml.core.Persister

class DcsJtacHubService : Service() {

    private val _binder = LocalBinder()
    private val _unitNames = MutableLiveData<List<String>>()
    private val _unitNamesSet = mutableSetOf<String>()
    private var _selectedUnitName: String? = null
    private lateinit var _locationMocker: LocationMocker
    private lateinit var _networkRepository: NetworkRepository

    val webSocketStatus = MutableLiveData<Int>(R.drawable.baseline_power_off_24)
    val unitNames: LiveData<List<String>> = _unitNames

    override fun onCreate() {
        super.onCreate()
        _networkRepository = NetworkRepository()
        _locationMocker = LocationMocker(this)
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

    fun connectToHub(url: String) {
        _networkRepository.connectToHub(url, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                webSocketStatus.postValue(R.drawable.baseline_power_24)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                webSocketStatus.postValue(R.drawable.baseline_satellite_alt_24)
                val unitName = extractUnitName(text)
                if (!unitName.isNullOrEmpty()) {
                    addUnitNameToSet(unitName)

                    // If the unit is the selected unit, update the device location
                    if (_selectedUnitName == unitName) {
                        val unit = Persister().read(CursorOnTarget::class.java, text)
                        _locationMocker.setMockLocation(unit.point.lat,unit.point.lon,unit.point.hae,0.0f)
                    // Otherwise, send it as a CoT. Additionally, avoid a CoT being placed on the user's location
                    } else if (!_selectedUnitName.isNullOrEmpty()) {
                        _networkRepository.sendCursorOnTarget(text)
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocketStatus.postValue(R.drawable.baseline_power_off_24)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                webSocketStatus.postValue(R.drawable.baseline_error_24)
            }
        })
    }

    fun setSelectedUnit(unitName: String) {
        _selectedUnitName = unitName
    }
    fun disconnectFromHub() {
        _networkRepository.disconnectFromHub()
    }

    private fun addUnitNameToSet(unitName: String) {
        synchronized(_unitNamesSet) {
            val added = _unitNamesSet.add(unitName)
            if (added) {
                val sortedList = _unitNamesSet.sorted()
                _unitNames.postValue(sortedList)
            }
            added
        }
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

    private fun extractUnitName(text: String): String? {
        val regex = Regex("uid=\"([a-zA-Z0-9_ -]+)\"")
        val match = regex.find(text)
        return match?.groups?.get(1)?.value
    }

    companion object {
        const val CHANNEL_ID = "DcsJtacHubChannel"
        const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): DcsJtacHubService = this@DcsJtacHubService
    }

}
