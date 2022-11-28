package com.daxtonb.dcsjtactoolsclient.service

import org.junit.Assert.*
import org.junit.Test

class DeviceIPAddressFinderTest {
    @Test
    fun findDeviceIPAddress() {
        // Arrange
        val regex = Regex("(\\d{1,3}\\.){3}\\d{1,3}")
        val service = DeviceIPAddressFinder()

        // Act
        var result = service.getIPAddress()

        // Assert
        assertNotNull(result)
        assertTrue(regex.matches(result!!))
    }
}