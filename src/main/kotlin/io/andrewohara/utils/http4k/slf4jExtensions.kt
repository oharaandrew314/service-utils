package io.andrewohara.utils.http4k

import org.http4k.appendIfPresent
import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.filter.ClientFilters
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.spi.MDCAdapter
import java.time.Clock
import java.time.Duration

fun ResponseFilters.logSummary(
    logger: Logger = LoggerFactory.getLogger("root"),
    clock: Clock = Clock.systemUTC(),
    shouldLog: (Request, Response) -> Boolean = { _, _ -> true }
) = Filter { next ->
    { request ->
        val start = clock.instant()
        val response = next(request)
        val duration = Duration.between(start, clock.instant())
        val source = request.header("X-Forwarded-For") ?: request.source?.address

        if (shouldLog(request, response)) {
            val message = StringBuilder()
                .append("${request.method} ${request.uri}")
                .append(": ${response.status}")
                .append(" in ${duration.toMillis()} ms")
                .appendIfPresent(source, " from $source")
                .toString()

            logger.info(message)
        }

        response
    }
}

fun ServerFilters.requestIdToMdc(
    key: String,
    mdc: MDCAdapter = MDC.getMDCAdapter(),
    generateRequestId: () -> String? = { null }
) = Filter { next ->
    { request ->
        mdc.clear()
        val requestId = request.header(key) ?: generateRequestId()
        if (requestId != null) {
            mdc.put(key, requestId)
            next(request).header(key, requestId)
        } else {
            next(request)
        }
    }
}

fun ClientFilters.mdcToRequestId(
    key: String,
    mdc: MDCAdapter = MDC.getMDCAdapter(),
    generateRequestId: () -> String? = { null }
) = Filter { next ->
    { request ->
        val requestId = mdc.get(key) ?: generateRequestId()

        val updated = if (requestId != null) {
            request.header(key, requestId)
        } else request

        next(updated)
    }
}

fun ServerFilters.logErrors(logger: Logger = LoggerFactory.getLogger("root")) = Filter { next ->
    { request ->
        try {
            next(request)
        } catch (e: Throwable) {
            logger.error("Error during $request", e)
            Response(Status.INTERNAL_SERVER_ERROR).body("Internal Server Error")
        }
    }
}