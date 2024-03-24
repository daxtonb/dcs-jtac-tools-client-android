import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daxtonb.dcsjtactoolsclient.NetworkRepository
import com.daxtonb.dcsjtactoolsclient.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MainViewModel(private val repository: NetworkRepository) : ViewModel() {
    private val _unitNames = MutableLiveData<List<String>>()
    val unitNames: LiveData<List<String>> = _unitNames

    private val _webSocketStatusIcon = MutableLiveData<Int>()
    val webSocketStatusIcon: LiveData<Int> = _webSocketStatusIcon

    fun connect(fullAddress: String) {
        repository.connectToHub(fullAddress, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                _webSocketStatusIcon.postValue(R.drawable.baseline_power_24)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                _webSocketStatusIcon.postValue(R.drawable.baseline_satellite_alt_24)
                repository.sendCursorOnTarget(text)
                extractAndAddUnitName(text)
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
        repository.disconnectFromHub()
    }

    private fun extractAndAddUnitName(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val regex = Regex("uid=\"([a-zA-Z0-9_-]+)\"")
            val match = regex.find(text)
            val unitName = match?.groups?.get(1)?.value
            if (!unitName.isNullOrEmpty()) {
                val updatedList = _unitNames.value?.toMutableList() ?: mutableListOf()
                if (!updatedList.contains(unitName)) {
                    updatedList.add(unitName)
                    updatedList.sort()
                    _unitNames.postValue(updatedList)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.closeConnections()
    }
}
