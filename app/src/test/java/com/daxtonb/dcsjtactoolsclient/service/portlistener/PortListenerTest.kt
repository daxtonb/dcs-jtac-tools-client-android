package com.daxtonb.dcsjtactoolsclient.service.portlistener

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PortListenerTest {
    @Test
    fun read_dataReturned() {
        // Arrange
        val data = "{\"key\":\"value\"}"
        val buffer = mockk<ScalingBuffer>()
        val port = 1
        every { buffer.readNext(any()) } answers { data }
        val listener = PortListener(buffer, port)

        // Act
        val result = listener.read()

        // Assert
        assertEquals(data, result)
        assertTrue(listener.isPortClosed)
    }

    @Test
    fun read_exceptionThrown_returnsEmptyString() {
        // Arrange
        val expected = ""
        val port = 1
        val buffer = mockk<ScalingBuffer>()
        every { buffer.readNext(any()) } answers { throw Exception() }
        val listener = PortListener(buffer, port)

        // Act
        val result = listener.read()

        // Assert
        assertEquals(expected, result)
        assertTrue(listener.isPortClosed)
    }
}