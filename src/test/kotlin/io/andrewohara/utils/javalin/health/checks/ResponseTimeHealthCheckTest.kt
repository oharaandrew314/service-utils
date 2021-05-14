package io.andrewohara.utils.javalin.health.checks

import org.assertj.core.api.Assertions
import org.junit.Test

class ResponseTimeHealthCheckTest {

    @Test
    fun `empty history should be healthy`() {
        val testObj = ResponseTimeHealthCheck(historySize = 10, requiredAverageResponseTime = 100)

        Assertions.assertThat(testObj.isHealthy()).isTrue
    }

    @Test
    fun `series of good response times`() {
        val testObj = ResponseTimeHealthCheck(historySize = 10, requiredAverageResponseTime = 100)

        testObj.addSamples(50, 40, 60, 30)

        Assertions.assertThat(testObj.isHealthy()).isTrue
    }

    @Test
    fun `series of bad response times`() {
        val testObj = ResponseTimeHealthCheck(historySize = 10, requiredAverageResponseTime = 100)

        testObj.addSamples(200, 4000, 350, 120)

        Assertions.assertThat(testObj.isHealthy()).isFalse
    }

    @Test
    fun `series of good and bad response times that average out to good`() {
        val testObj = ResponseTimeHealthCheck(historySize = 10, requiredAverageResponseTime = 100)

        testObj.addSamples(30, 200, 40, 25)

        Assertions.assertThat(testObj.isHealthy()).isTrue
    }

    @Test
    fun `series of good response times that get pushed off history be bad ones`() {
        val testObj = ResponseTimeHealthCheck(historySize = 4, requiredAverageResponseTime = 100)

        testObj.addSamples(10, 15, 8, 20, 200, 150, 110, 340)

        Assertions.assertThat(testObj.isHealthy()).isFalse
    }

    private fun ResponseTimeHealthCheck.addSamples(vararg responseTimes: Long) = responseTimes.forEach { addSample(it) }
}