package com.daxtonb.dcsjtactoolsclient.service

import android.util.Log
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
// See https://stackoverflow.com/questions/26402218/how-to-get-the-ip-address-of-an-android-mobile-programatically

class DeviceIPAddressProvider {
    fun getIPAddress(): String? {
        try {
            return findDeviceIPAddress()
        } catch (ex: java.lang.Exception) {
            Log.e("getIPAddress", "Error getting IP address", ex)
        }
        return null
    }

    private fun findDeviceIPAddress(): String? {
        val networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
        networkInterfaces.forEach {
            val addresses: List<InetAddress> = Collections.list(it.inetAddresses)
            val deviceIPAddress = searchInetAddressesForDeviceIP(addresses)
            if (deviceIPAddress != null) return deviceIPAddress
        }
        return null
    }

    private fun searchInetAddressesForDeviceIP(addresses: List<InetAddress>): String? {
        addresses.forEach {
            val hostAddress: String = it.hostAddress
                ?: throw Exception("Host address not found.")
            val isIPv4 = hostAddress.indexOf(':') < 0
            if (isIPv4) return hostAddress
        }
        return null
    }
}