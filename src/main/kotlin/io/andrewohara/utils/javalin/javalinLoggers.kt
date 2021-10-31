package io.andrewohara.utils.javalin

import io.andrewohara.utils.IdGenerator
import io.javalin.Javalin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.Duration
import java.time.Instant

fun Javalin.withRequestId(
    headerName: String = "Request-Id",
    attributeName: String = headerName,
    generateIfMissing: Boolean = true
) {
    before { context ->
        val requestId = context.header(headerName)
            ?: if (generateIfMissing) IdGenerator.nextBase36(8) else return@before

        context.attribute(attributeName, requestId)
        MDC.put(attributeName, requestId)
    }

    after {
        MDC.clear()
    }
}

fun Javalin.logErrors(log: Logger = LoggerFactory.getLogger(javaClass)) {
    exception(Exception::class.java) { e, ctx ->
        log.error("Unhandled Exception", e)
        ctx.status(500)
    }
}

fun Javalin.logResponses(log: Logger = LoggerFactory.getLogger(javaClass)) {
    before { context ->
        context.attribute("requestStart", Instant.now())
    }

    after { context ->
        val requestDuration = Duration.between(context.attribute<Instant>("requestStart"), Instant.now())

        context.header("Request-ID", context.attribute<String>("requestId") ?: "empty")
        log.info("Method=${context.method()} Path=${context.path()} Status=${context.status()}  Latency=${requestDuration.toMillis()}ms Caller=${context.header("X-Forwarded-For")}")
    }
}