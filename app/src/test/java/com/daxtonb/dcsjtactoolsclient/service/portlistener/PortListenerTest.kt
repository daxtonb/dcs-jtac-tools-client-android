package com.daxtonb.dcsjtactoolsclient.service.portlistener

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class PortListenerTest {
    @Test
    fun closeConnection_nullSocket_noErrorThrown() {
        // Arrange
        val listener = PortListener(1) { }

        // Act
        listener.closeConnection()

        // Assert
        assertTrue(true)
    }

    @Test
    fun stop_notListening() {
        // Arrange
        val listener = PortListener(1) { }

        // Act
        listener.stop()

        // Assert
        assertFalse(listener.isListening)
    }

    @Test
    fun read_dataReturned_isStillListening() {
        // Arrange
        val data = "{\"key\":\"value\"}"
        val listener = PortListener(1) { }
        val buffer = mockk<ScalingBuffer>()
        every {
            buffer.readNext(any())
        } answers {
            data
        }

        // Act
        val result = listener.read(buffer)

        // Assert
        assertEquals(data, result)
        assertTrue(listener.isListening)
    }

    @Test
    fun read_ExceptionThrown_isStillListening()
    {
        // Arrange
        val listener = PortListener(1) { }
        val buffer = mockk<ScalingBuffer>()
        every {
            buffer.readNext(any())
        } answers {
            throw Exception()
        }

        // Act & Assert
        assertThrows(Exception::class.java) { listener.read(buffer) }
        assertTrue(listener.isListening)
    }
}