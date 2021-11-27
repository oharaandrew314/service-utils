package io.andrewohara.utils.http4k

import org.http4k.core.Filter
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.filter.ClientFilters
import org.http4k.filter.ServerFilters
import org.slf4j.Logger
import org.slf4j.MDC
import org.slf4j.spi.MDCAdapter
import java.time.Clock
import java.time.Duration

fun ServerFilters.logResponseStatus(
    logger: Logger,
    clock: Clock = Clock.systemUTC(),
    shouldLog: (Request, Response) -> Boolean = { _, _ -> true }
) = Filter { next ->
    { request ->
        val start = clock.instant()
        val response = next(request)
        val duration = Duration.between(start, clock.instant())

        if (shouldLog(request, response)) {
            logger.info("${request.method} ${request.uri}: ${response.status} in ${duration.toMillis()} ms")
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
        mdc.put(key, request.header(key) ?: generateRequestId())

        next(request)
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

fun ServerFilters.logErrors(logger: Logger) = Filter { next ->
    { request ->
        try {
            next(request)
        } catch (e: Throwable) {
            logger.error("Error during $request", e)
            throw e
        }
    }
}