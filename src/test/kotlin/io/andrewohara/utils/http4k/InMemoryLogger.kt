package io.andrewohara.utils.http4k

import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.AbstractLogger

data class LogMessage(
    val level: Level,
    val message: String
)

class InMemoryLogger(val lines: MutableList<LogMessage> = mutableListOf()
): AbstractLogger(), Iterable<LogMessage> by lines {

    override fun isTraceEnabled() = true
    override fun isTraceEnabled(marker: Marker?) = true

    override fun isDebugEnabled() = true
    override fun isDebugEnabled(marker: Marker?) = true

    override fun isInfoEnabled() = true
    override fun isInfoEnabled(marker: Marker?) = true

    override fun isWarnEnabled() = true
    override fun isWarnEnabled(marker: Marker?) = true

    override fun isErrorEnabled() = true
    override fun isErrorEnabled(marker: Marker?) = true

    override fun getFullyQualifiedCallerName() = "qualify this"

    override fun handleNormalizedLoggingCall(level: Level, marker: Marker?, msg: String, arguments: Array<out Any>?, throwable: Throwable?) {
        lines += LogMessage(level, msg)
    }
}