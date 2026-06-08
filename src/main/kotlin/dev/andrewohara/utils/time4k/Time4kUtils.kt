package dev.andrewohara.utils.time4k

import dev.forkhandles.time.TimeSource
import java.time.Clock
import java.time.ZoneId
import java.time.ZoneOffset

fun TimeSource.toClock(zone: ZoneId = ZoneOffset.UTC): Clock = object: Clock() {
    override fun getZone() = zone
    override fun withZone(zone: ZoneId) = this@toClock.toClock(zone)
    override fun instant() = invoke()
}