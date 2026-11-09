package com.specure.track.data.ping

import app.cash.turbine.test
import com.marsounjan.icmp4a.Icmp
import com.marsounjan.icmp4a.Icmp4a
import com.specure.track.domain.ping.PingRunnerStatus
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.InetAddress

class PingIcmp4aDataSourceTest {

    private val mockIcmp4a = mockk<Icmp4a>()
    private val dataSource = PingIcmp4aDataSource(mockIcmp4a)
    private val testAddress = InetAddress.getByName("8.8.8.8")

    @Test
    fun `observePing transformation to PingRunnerState`() = runTest {
        val response1 = Icmp.PingStatus(
            ip = testAddress,
            packetsTransmitted = 1,
            packetsReceived = 1,
            packetLoss = 0f,
            stats = Icmp.LatencyStats(minMs = 10, avgMs = 10, maxMs = 10),
            result = Icmp.PingResult.Success(packetSize = 64, ms = 10, sequenceNumber = 1)
        )

        val response2 = Icmp.PingStatus(
            ip = testAddress,
            packetsTransmitted = 2,
            packetsReceived = 2,
            packetLoss = 0f,
            stats = Icmp.LatencyStats(minMs = 10, avgMs = 15, maxMs = 20),
            result = Icmp.PingResult.Success(packetSize = 64, ms = 20, sequenceNumber = 2)
        )

        val response3 = Icmp.PingStatus(
            ip = testAddress,
            packetsTransmitted = 3,
            packetsReceived = 2,
            packetLoss = 0.33f,
            stats = Icmp.LatencyStats(minMs = 10, avgMs = 15, maxMs = 20),
            result = Icmp.PingResult.Failed.RequestTimeout(message = "Request timeout", millis = 1000L)
        )

        every {
            mockIcmp4a.pingInterval(host = "8.8.8.8", count = null, intervalMillis = 1000)
        } returns flowOf(response1, response2, response3)

        // WHEN & THEN
        dataSource.observePing(host = "8.8.8.8", intervalMillis = 1000).test {

            val state1 = awaitItem()
            assertNotNull(state1.pingStats)
            assertEquals(1, state1.pingStats?.packetsTransmitted)
            assertEquals(10L, state1.pingStats?.stats?.avgMs)
            assertEquals(PingRunnerStatus.Running, state1.pingRunnerStatus)

            val state2 = awaitItem()
            assertEquals(2, state2.pingStats?.packetsTransmitted)
            assertEquals(15L, state2.pingStats?.stats?.avgMs)
            assertEquals(PingRunnerStatus.Running, state2.pingRunnerStatus)

            val state3 = awaitItem()
            assertEquals(3, state3.pingStats?.packetsTransmitted)
            assertEquals(2, state3.pingStats?.packetsReceived)
            assertEquals(0.33f, state3.pingStats?.packetLoss ?: 0f, 0.01f)

            assertTrue(state3.pingRunnerStatus is PingRunnerStatus.Error)
            val errorStatus = state3.pingRunnerStatus as PingRunnerStatus.Error
            assertEquals("Request timeout", errorStatus.exception.message)

            awaitComplete()
        }
    }
}
