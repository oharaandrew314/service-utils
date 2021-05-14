package io.andrewohara.utils.javalin

import io.andrewohara.utils.IdGenerator
import io.andrewohara.utils.javalin.health.checks.HealthCheck
import io.andrewohara.utils.javalin.health.HealthCheckApi
import io.andrewohara.utils.javalin.health.HealthCheckHandler
import io.javalin.Javalin
import io.javalin.core.security.Role
import org.apache.log4j.MDC
import org.slf4j.Logger
import java.time.Duration
import java.time.Instant

object JavalinUtils {

    fun registerInstrumentation(log: Logger, app: Javalin) {
        app.exception(Exception::class.java) { e, ctx ->
            log.error("Unhandled Exception", e)
            ctx.status(500)
        }

        app.before { context ->
            val requestId = context.header("Request-Id") ?: IdGenerator.nextBase36(8)
            context.attribute("requestId", requestId)
            context.attribute("requestStart", Instant.now())
            MDC.put("requestId", requestId)
        }
        app.after("*") { context ->
            val requestDuration = Duration.between(context.attribute<Instant>("requestStart"), Instant.now())

            context.header("Request-ID", context.attribute<String>("requestId") ?: "empty")
            log.info("Method=${context.method()} Path=${context.path()} Status=${context.status()}  Latency=${requestDuration.toMillis()}ms Caller=${context.header("X-Forwarded-For")}")
            MDC.clear()
        }
    }

    fun registerHealthCheck(app: Javalin, healthChecks: Collection<HealthCheck>, allowedRoles: Set<Role> = emptySet()) {
        val handler = HealthCheckHandler(healthChecks)
        HealthCheckApi(app, handler, allowedRoles)
    }
}