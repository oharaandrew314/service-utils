package dev.andrewohara.utils.jdk

import java.time.*

data class MutableFixedClock(private var time: Instant, private val zone: ZoneId = ZoneOffset.UTC): Clock() {
    override fun getZone() = zone
    override fun withZone(zone: ZoneId) = copy(zone = zone)
    override fun instant() = time

    operator fun plus(duration: Duration): Instant {
        time += duration
        return time
    }

    operator fun minus(duration: Duration): Instant {
        time -= duration
        return time
    }

    operator fun plusAssign(duration: Duration) {
        time += duration
    }

    operator fun minusAssign(duration: Duration) {
        time -= duration
    }
}

fun Instant.toClock(zone: ZoneId = ZoneOffset.UTC) = MutableFixedClock(this, zone)