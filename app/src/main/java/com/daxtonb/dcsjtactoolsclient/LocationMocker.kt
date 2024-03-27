package com.daxtonb.dcsjtactoolsclient

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.SystemClock

class LocationMocker(private val context: Context) {

    private val _providers = arrayOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    private val _locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        _providers.forEach { _locationManager.addTestProvider(
            it,
            false, false, false,
            false, true, true, true,
            ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE
        )
            _locationManager.setTestProviderEnabled(it, true) }
    }

    fun setMockLocation(latitude: Double, longitude: Double, altitude: Double, bearing: Float) {
        try {
            _providers.forEach { val mockLocation = Location(it)
                mockLocation.latitude = latitude
                mockLocation.longitude = longitude
                mockLocation.altitude = altitude
                mockLocation.bearing = bearing
                mockLocation.bearingAccuracyDegrees = 0.1f
                mockLocation.verticalAccuracyMeters = 0.1f
                mockLocation.speedAccuracyMetersPerSecond = 0.1f
                mockLocation.time = System.currentTimeMillis()
                mockLocation.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                mockLocation.accuracy = 3f

                _locationManager.setTestProviderLocation(it, mockLocation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
