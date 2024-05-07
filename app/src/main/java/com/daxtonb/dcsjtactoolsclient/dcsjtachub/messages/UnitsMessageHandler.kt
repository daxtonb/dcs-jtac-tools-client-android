package com.daxtonb.dcsjtactoolsclient.dcsjtachub.messages

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.daxtonb.dcsjtactoolsclient.CursorOnTarget
import com.daxtonb.dcsjtactoolsclient.LocationMocker
import com.daxtonb.dcsjtactoolsclient.NetworkRepository
import org.simpleframework.xml.core.Persister

class UnitsMessageHandler(
    private val _networkRepository: NetworkRepository,
    private val _selectedUnitName: MutableLiveData<String?>,
    private val _unitNamesSet: MutableSet<String>,
    private val _unitNames: MutableLiveData<List<String>>,
    context: Context
) : HubMessageHandlerBase() {

    private var _locationMocker: LocationMocker? = null

    init {
        try {
            _locationMocker = LocationMocker(context)
        } catch (e: SecurityException) {
            println("Location mocks are not enabled")
        }
    }

    override fun getTopic(): String {
        return "UNITS"
    }

    override fun processMessage(message: String) {
        val xml = extractMessageBody(message)
        val unitName = extractUnitName(xml)
        if (!unitName.isNullOrEmpty()) {
            addUnitNameToSet(unitName)

            // If the unit is the selected unit, update the device location
            if (_selectedUnitName.value == unitName && _locationMocker != null) {
                val unit = Persister().read(CursorOnTarget::class.java, xml)
                _locationMocker?.setMockLocation(
                    unit.point.lat,
                    unit.point.lon,
                    unit.point.hae,
                    0.0f
                )
                // Otherwise, send it as a CoT. Additionally, avoid a CoT being placed on the user's location
            } else if (!_selectedUnitName.value.isNullOrEmpty() || _locationMocker == null) {
                _networkRepository.sendCursorOnTarget(xml)
            }
        }
    }

    override fun dispose() {
        _locationMocker?.dispose()
    }

    private fun extractUnitName(text: String): String? {
        val regex = Regex("uid=\"([-a-zA-Z0-9_ #]+)\"")
        val match = regex.find(text)
        return match?.groups?.get(1)?.value
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
}