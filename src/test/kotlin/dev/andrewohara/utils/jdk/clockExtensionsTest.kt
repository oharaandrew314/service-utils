package dev.andrewohara.utils.jdk

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class clockExtensionsTest {

    private val testObj = Clock.fixed(Instant.parse("2021-01-01T12:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `get with plus offset`() {
        testObj + Duration.ofSeconds(10) shouldBe Instant.parse("2021-01-01T12:00:10Z")
    }

    @Test
    fun `get with minus offset`() {
        testObj - Duration.ofSeconds(10) shouldBe Instant.parse("2021-01-01T11:59:50Z")
    }
}