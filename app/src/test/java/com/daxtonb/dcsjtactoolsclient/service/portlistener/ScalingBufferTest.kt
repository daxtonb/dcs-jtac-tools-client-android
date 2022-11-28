package com.daxtonb.dcsjtactoolsclient.service.portlistener

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.*
import org.junit.Test
import java.net.DatagramPacket
import java.net.DatagramSocket

class ScalingBufferTest {
    @Test
    fun deserializeData_buildsJson() {
        // Arrange
        val json = "{\"key\":\"value\"}"
        val byteArray = json.toByteArray()
        val packet = DatagramPacket(byteArray, byteArray.size)
        val buffer = ScalingBuffer("")

        // Act
        val result = buffer.deserializeData(packet)

        // Assert
        assertEquals(json, result)
    }

    @Test
    fun allDataIsInBuffer_caseTrue() {
        // Arrange
        val json = "{\"key\":\"value\"}"
        val buffer = ScalingBuffer("}")

        // Act
        val result = buffer.allDataIsInBuffer(json)

        // Assert
        assertTrue(result)
    }

    @Test
    fun allDataIsInBuffer_caseFalse() {
        // Arrange
        val json = "{\"key\":\"value\""
        val buffer = ScalingBuffer("}")

        // Act
        val result = buffer.allDataIsInBuffer(json)

        // Assert
        assertFalse(result)
    }

    @Test
    fun scaleBufferToDataSize_noData_sizeDoesNotChange() {
        // Arrange
        val data = ""
        val buffer = ScalingBuffer("}")

        // Act
        buffer.scaleBufferToDataSize(data)

        // Assert
        assertEquals(buffer.defaultBufferSize, buffer.currentBufferSize)
    }

    @Test
    fun scaleBufferToDataSize_allDataIsInBuffer_sizeChanged() {
        // Arrange
        val data = "{\"key\":\"value\"}"
        val buffer = ScalingBuffer("}")

        // Act
        buffer.scaleBufferToDataSize(data)

        // Assert
        assertEquals(data.length, buffer.currentBufferSize)
    }

    @Test
    fun scaleBufferToDataSize_notAllDataIsInBuffer_sizeChanged() {
        // Arrange
        val data = "{\"key\":\"value\""
        val buffer = ScalingBuffer("}")
        val expectedBufferSize = buffer.defaultBufferSize * 2

        // Act
        buffer.scaleBufferToDataSize(data)

        // Assert
        assertEquals(expectedBufferSize, buffer.currentBufferSize)
    }

    @Test
    fun buildReturnValue_allDataIsInBuffer_dataReturned() {
        // Arrange
        val data = "{\"key\":\"value\"}"
        val buffer = ScalingBuffer("}")

        // Act
        val result = buffer.buildReturnValue(data)

        // Assert
        assertEquals(data, result)
    }

    @Test
    fun buildReturnValue_allDataIsNotInBuffer_nothingReturned() {
        // Arrange
        val data = "{\"key\":\"value\""
        val buffer = ScalingBuffer("}")
        val expectedValue = ""

        // Act
        val result = buffer.buildReturnValue(data)

        // Assert
        assertEquals(expectedValue, result)
    }

    @Test
    fun readNext_allDataReceived_returnsJson() {
        // Arrange
        val data = "{\"key\":\"value\"}"
        val slot = slot<DatagramPacket>()
        val socket = mockk<DatagramSocket>()
        val buffer = ScalingBuffer("}")

        every {
            socket.receive(capture(slot))
        } answers {
            slot.captured.data = data.toByteArray()
        }

        // Act
        var result = buffer.readNext(socket)

        // Assert
        assertEquals(data, result)

    }

    @Test
    fun readNext_incompleteDataReceived_returnsJson() {
        // Arrange
        val data = "{\"key\":\"value\""
        val slot = slot<DatagramPacket>()
        val socket = mockk<DatagramSocket>()
        val buffer = ScalingBuffer("}")
        val expectedResult = ""

        every {
            socket.receive(capture(slot))
        } answers {
            slot.captured.data = data.toByteArray()
        }

        // Act
        var result = buffer.readNext(socket)

        // Assert
        assertEquals(expectedResult, result)

    }
}