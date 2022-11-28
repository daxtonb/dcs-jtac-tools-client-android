package com.daxtonb.dcsjtactoolsclient.service

import android.content.Context
import android.location.LocationManager
import com.daxtonb.dcsjtactoolsclient.model.UnitLocation
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test

class DeviceLocationServiceTest {
    private val _expectedProviders = listOf<String>(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER)

    @Test
    fun setLocation_testProvidersSet() {
        // Arrange
        val mockData = buildMockData()
        val context = mockData.first
        val manager = mockData.second

        val unitLocation = UnitLocation()
        unitLocation.lat = 1.0
        unitLocation.long = 2.0
        unitLocation.alt = 3.0
        unitLocation.head = 4.0

        val service = DeviceLocationService(context)

        // Act
        service.setLocation(unitLocation)

        // Assert
        verify {
            manager.setTestProviderEnabled(withArg {
                    assertTrue(_expectedProviders.contains(it))
                }, withArg {
                    assertTrue(it)
            })
        }

        verify {
            manager.setTestProviderLocation(withArg {
                    assertTrue(_expectedProviders.contains(it))
                }, withArg {
                    // This is really something we need to test, but low-level dependencies
                    // are making Location impossible to use within the unit test environment
//                    assertEquals(unitLocation.lat, it.latitude, 0.0)
//                    assertEquals(unitLocation.long, it.longitude, 0.0)
//                    assertEquals(unitLocation.head, it.bearing)
//                    assertEquals(unitLocation.alt, it.altitude, 0.0)
            })
        }
    }

    @Test
    fun deactivate_testProvidersRemovedWhenSet() {
        // Arrange
        val mockData = buildMockData()
        val context = mockData.first
        val manager = mockData.second
        val unitLocation = UnitLocation()
        val service = DeviceLocationService(context)
        every { manager.allProviders } returns _expectedProviders
        service.setLocation(unitLocation)

        // Act
        service.deactivate()

        // Assert
        verify(exactly = _expectedProviders.size) {
            manager.removeTestProvider(withArg {
                assertTrue(_expectedProviders.contains(it))
            })
        }
    }

    @Test
    fun deactivate_noTestProvidersRemovedWhenNotSet() {
        // Arrange
        val mockData = buildMockData()
        val context = mockData.first
        val manager = mockData.second
        val service = DeviceLocationService(context)
        every { manager.allProviders } returns _expectedProviders

        // Act
        service.deactivate()

        // Assert
        verify(inverse = true) { manager.removeTestProvider(any()) }
    }

    private fun buildMockData(): Pair<Context, LocationManager> {
        val mockLocationManager = mockk<LocationManager>(relaxed = true)
        val mockContext = mockk<Context>(relaxed = true)

        every {
            mockContext.getSystemService(any())
        } answers {
            mockLocationManager
        }

        return Pair(mockContext, mockLocationManager)
    }
}