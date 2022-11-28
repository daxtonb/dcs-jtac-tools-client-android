package com.daxtonb.dcsjtactoolsclient.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import android.util.Log
import com.daxtonb.dcsjtactoolsclient.model.UnitLocation

class DeviceLocationService(context: Context) {
    private var _locationManager: LocationManager
    private var _isProvidersSet = false
    private val _providers = arrayOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

    init {
        _locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun setLocation(unitLocation: UnitLocation) {
        Log.d("DeviceLocationService","Setting unit location - $unitLocation")
        if (!_isProvidersSet) addTestProviders()
        _providers.forEach { setLocation(it, unitLocation) }
    }

    fun deactivate() {
        if (_isProvidersSet) removeTestProviders()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setLocation(provider: String, unitLocation: UnitLocation) {
        val location = Location(provider)

        location.time = System.currentTimeMillis()
        location.latitude = unitLocation.lat
        location.longitude = unitLocation.long
        location.altitude = unitLocation.alt
        location.bearing = unitLocation.head.toFloat()
        location.accuracy = 3F

        // Only necessary for unit testing
        if (Build.VERSION.SDK_INT >= 17)
            location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            location.bearingAccuracyDegrees = 0.1F
            location.verticalAccuracyMeters = 0.1F
            location.speedAccuracyMetersPerSecond = 0.01F
        }

        _locationManager.setTestProviderLocation(provider, location)
    }

    @SuppressLint("InlinedApi")
    private fun addTestProviders() {
        _providers.forEach {
            if (!_locationManager.allProviders.contains(it)) {
                _locationManager.addTestProvider(it,
                    false,
                    false,
                    false,
                    false,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_LOW,
                    ProviderProperties.ACCURACY_FINE)
            }
            if (!_locationManager.isProviderEnabled(it))
                _locationManager.setTestProviderEnabled(it, true)
            _isProvidersSet = true
        }
    }

    private fun removeTestProviders() {
        _providers.forEach {
            if (_locationManager.allProviders.contains(it))
                _locationManager.removeTestProvider(it)
        }
        _isProvidersSet = false
    }
}