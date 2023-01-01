package com.daxtonb.dcsjtactoolsclient.service.portlistener

import android.util.Log
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.system.exitProcess

class PortListenerRunnerTest {

    @Test
    fun run_dataReturned_stillListening() {
        // Arrange
        var runner: PortListenerRunner? = null
        var future: Future<*>? = null
        val executorService = Executors.newSingleThreadExecutor()
        val data = "{\"key\":\"value\"}"
        val listener = mockk<PortListener>()
        every { listener.read() } answers { data }

        runner = PortListenerRunner(listener) {
            // Assert
            assertEquals(data, it)
            assertTrue(runner?.isListening ?: false)
            verify(exactly = 0) { listener.closePort() }
            future?.cancel(true)
        }

        // Act
        future = executorService.submit(runner)
    }
    
    @Test
    fun stop_listeningToggled_closePort() {
        // Arrange
        var runner: PortListenerRunner? = null
        var future: Future<*>? = null
        val executorService = Executors.newSingleThreadExecutor()
        val data = "{\"key\":\"value\"}"
        val listener = mockk<PortListener>()
        every { listener.read() } answers { data }

        runner = PortListenerRunner(listener) {
            // Assert
            runner?.stop()
            assertFalse(runner?.isListening ?: true)
            verify(exactly = 1) { listener.closePort() }
            future?.cancel(true)
        }

        // Act
        future = executorService.submit(runner)
    }
}