package io.andrewohara.utils.javalin.health.checks

import org.assertj.core.api.Assertions
import org.junit.Test

class StatusHealthCheckTest {

    @Test
    fun `empty history should be healthy`() {
        val testObj = StatusHealthCheck(historySize = 10, requiredSuccessRatio = 0.8f)

        Assertions.assertThat(testObj.isHealthy()).isTrue
    }

    @Test
    fun `add partial history with multiple status codes that should be healthy`() {
        val testObj = StatusHealthCheck(historySize = 10, requiredSuccessRatio = 0.8f)

        testObj.addSamples(200, 404, 301, 200, 500)

        Assertions.assertThat(testObj.isHealthy()).isTrue
    }

    @Test
    fun `add partial history with just enough 5xx statuses to be unhealthy`() {
        val testObj = StatusHealthCheck(historySize = 10, requiredSuccessRatio = 0.8f)

        testObj.addSamples(504, 200, 301, 200, 500)

        Assertions.assertThat(testObj.isHealthy()).isFalse
    }

    @Test
    fun `overfill history such that a history that was healthy becomes unhealthy`() {
        val testObj = StatusHealthCheck(historySize = 4, requiredSuccessRatio = 0.5f)

        testObj.addSamples(200, 200, 200, 500, 500, 500)

        Assertions.assertThat(testObj.isHealthy()).isFalse
    }

    private fun StatusHealthCheck.addSamples(vararg samples: Int) = samples.forEach { addSample(it) }
}