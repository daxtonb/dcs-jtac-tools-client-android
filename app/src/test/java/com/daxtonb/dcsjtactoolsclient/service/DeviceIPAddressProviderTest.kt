package com.daxtonb.dcsjtactoolsclient.service

import org.junit.Assert.*
import org.junit.Test

class DeviceIPAddressProviderTest {
    @Test
    fun findDeviceIPAddress() {
        // Arrange
        val regex = Regex("(\\d{1,3}\\.){3}\\d{1,3}")
        val provider = DeviceIPAddressProvider()

        // Act
        val result = provider.getIPAddress()

        // Assert
        assertNotNull(result)
        assertTrue(regex.matches(result!!))
    }
}