package com.daxtonb.dcsjtactoolsclient
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.simpleframework.xml.core.Persister

class MainViewModel() : ViewModel() {
    private val _repository = NetworkRepository()
    private val _unitNamesSet = mutableSetOf<String>()
    private val _unitNames = MutableLiveData<List<String>>()
    private val _webSocketStatusIcon = MutableLiveData<Int>()
    private  val _selectedUnitName = MutableLiveData<String?>()

    val webSocketStatusIcon: LiveData<Int> = _webSocketStatusIcon
    val unitNames: LiveData<List<String>> = _unitNames
    val selectedUnitName: LiveData<String?> = _selectedUnitName

    fun connect(fullAddress: String, locationMocker: LocationMocker) {
        _repository.connectToHub(fullAddress, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                _webSocketStatusIcon.postValue(R.drawable.baseline_power_24)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _webSocketStatusIcon.postValue(R.drawable.baseline_satellite_alt_24)
                _repository.sendCursorOnTarget(text)
                val unitName = extractUnitName(text)
                if (!unitName.isNullOrEmpty()) {
                    addUnitNameToSet(unitName)
                    if (_selectedUnitName.value == unitName) {
                        val unit = Persister().read(CursorOnTarget::class.java, text)
                        locationMocker.setMockLocation(unit.point.lat,unit.point.lon,unit.point.hae,0.0f)
                    }
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _webSocketStatusIcon.postValue(R.drawable.baseline_power_off_24)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                _webSocketStatusIcon.postValue(R.drawable.baseline_error_24)
            }
        })
    }

    fun disconnect() {
        _repository.disconnectFromHub()
    }

    private fun extractUnitName(text: String): String? {
        val regex = Regex("uid=\"([a-zA-Z0-9_-]+)\"")
        val match = regex.find(text)
        return match?.groups?.get(1)?.value
    }

    private fun addUnitNameToSet(unitName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val updated = synchronized(_unitNamesSet) {
                val added = _unitNamesSet.add(unitName)
                if (added) {
                    val sortedList = _unitNamesSet.sorted()
                    _unitNames.postValue(sortedList)
                }
                added
            }
        }
    }

    fun setSelectedUnitName(unitName: String) {
        _selectedUnitName.value = unitName
    }

    override fun onCleared() {
        super.onCleared()
        _repository.closeConnections()
    }
}
