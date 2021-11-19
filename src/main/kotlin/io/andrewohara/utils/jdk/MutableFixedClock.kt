package io.andrewohara.utils.jdk

import java.time.*

data class MutableFixedClock(private var time: Instant, private val zone: ZoneId = ZoneOffset.UTC): Clock() {
    override fun getZone() = zone
    override fun withZone(zone: ZoneId) = copy(zone = zone)
    override fun instant() = time

    operator fun plusAssign(duration: Duration) {
        time += duration
    }

    operator fun minusAssign(duration: Duration) {
        time -= duration
    }
}