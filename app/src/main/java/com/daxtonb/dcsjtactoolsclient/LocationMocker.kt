package com.daxtonb.dcsjtactoolsclient

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast

class LocationMocker(private val context: Context) {

    private val _providers = arrayOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
    private val _locationManager: LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        _providers.forEach {
            _locationManager.addTestProvider(
                it,
                false, false, false,
                false, true, true, true,
                ProviderProperties.POWER_USAGE_LOW, ProviderProperties.ACCURACY_FINE
            )
            _locationManager.setTestProviderEnabled(it, true)
        }
    }

    fun setMockLocation(latitude: Double, longitude: Double, altitude: Double, bearing: Float) {
        try {
            _providers.forEach {
                val mockLocation = Location(it)
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

    fun dispose() {
        _providers.forEach {
            _locationManager.removeTestProvider(it)
        }
    }

    companion object {
        fun checkMockLocationEnabled(context: Context): Boolean {
            return try {
                val locationMocker = LocationMocker(context)
                locationMocker.dispose()
                true
            } catch (e: SecurityException) {
                promptUserForMockLocation(context)
                false
            }
        }

        private fun promptUserForMockLocation(context: Context) {
            AlertDialog.Builder(context).apply {
                setTitle("Enable Mock Locations")
                setMessage("Mock locations are not enabled. This is required if you wish to sync your GPS location with DCS. Please enable it in Developer Options and restart DCS JTAC Tools Client.")
                setPositiveButton("Go to Settings") { _, _ ->
                    redirectToDeveloperOptions(context)
                }
                setNegativeButton("Cancel", null)
                show()
            }
        }

        private fun redirectToDeveloperOptions(context: Context) {
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Could not open Developer Options. Please enable mock locations manually.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

