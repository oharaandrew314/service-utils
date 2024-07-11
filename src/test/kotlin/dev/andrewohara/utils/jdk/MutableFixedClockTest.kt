package dev.andrewohara.utils.jdk

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.*

class MutableFixedClockTest {

    private val testObj = Instant.parse("2021-01-01T12:00:00Z").toClock()

    @Test
    fun `ZonedDateTime at UTC`() {
        ZonedDateTime.now(testObj) shouldBe ZonedDateTime.of(2021, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC)
    }

    @Test
    fun `ZonedDateTime at EST`() {
        val testObj = Instant.parse("2021-01-01T12:00:00Z").toClock(ZoneOffset.of("-05:00"))

        ZonedDateTime.now(testObj) shouldBe ZonedDateTime.of(2021, 1, 1, 7, 0, 0, 0, ZoneOffset.of("-05:00"))
    }

    @Test
    fun `with new zone`() {
        val updated = testObj.withZone(ZoneOffset.of("-05:00"))
        updated.zone shouldBe ZoneOffset.of("-05:00")
    }

    @Test
    fun `increment time`() {
        testObj += Duration.ofHours(1)

        testObj.instant() shouldBe Instant.parse("2021-01-01T13:00:00Z")
    }

    @Test
    fun `decrement time`() {
        testObj -= Duration.ofHours(1)

        testObj.instant() shouldBe Instant.parse("2021-01-01T11:00:00Z")
    }

    @Test
    fun `increment and return time`() {
        val time = testObj + Duration.ofHours(1)

        time shouldBe Instant.parse("2021-01-01T13:00:00Z")
        testObj.instant() shouldBe Instant.parse("2021-01-01T13:00:00Z")
    }

    @Test
    fun `Decrement and return time`() {
        val time = testObj - Duration.ofHours(1)

        time shouldBe Instant.parse("2021-01-01T11:00:00Z")
        testObj.instant() shouldBe Instant.parse("2021-01-01T11:00:00Z")
    }
}