package dev.andrewohara.utils.jdk

import java.io.Closeable
import java.time.*

data class MutableFixedClock(
    private var time: Instant,
    private val zone: ZoneId = ZoneOffset.UTC
): Clock() {

    private val subscriptions = mutableListOf<(Instant) -> Unit>()

    override fun getZone() = zone
    override fun withZone(zone: ZoneId) = copy(zone = zone)
    override fun instant() = time

    operator fun plus(duration: Duration): Instant {
        time += duration
        notifySubscribers()
        return time
    }

    operator fun minus(duration: Duration): Instant {
        time -= duration
        notifySubscribers()
        return time
    }

    operator fun plusAssign(duration: Duration) {
        time += duration
        notifySubscribers()
    }

    operator fun minusAssign(duration: Duration) {
        time -= duration
        notifySubscribers()
    }

    fun subscribe(subscription: (Instant) -> Unit): Closeable {
        subscriptions += subscription
        return Closeable { subscriptions -= subscription }
    }

    private fun notifySubscribers() {
        subscriptions.forEach { it(time) }
    }
}

fun Instant.toClock(zone: ZoneId = ZoneOffset.UTC) = MutableFixedClock(this, zone)