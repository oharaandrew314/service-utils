package io.andrewohara.utils.javalin.health

import io.andrewohara.utils.javalin.health.checks.HealthCheck
import org.apache.log4j.Logger
import java.lang.Exception

class HealthCheckHandler(private val healthChecks: Collection<HealthCheck>) {

    private val log = Logger.getLogger(javaClass)

    fun check(): Result {
        val results = healthChecks.map {
                val name = it.javaClass.simpleName
                val healthy = try {
                    it.isHealthy()
                } catch (e: Exception) {
                    log.warn("Error while getting health for $name", e)
                    false
                }
                name to healthy
            }
            .toMap()

        return Result(results)
    }

    data class Result(
        val results: Map<String, Boolean>,
    ) {
        val healthy = results.count { it.value }
        val unhealthy = results.count { !it.value }
        val total = results.size
        val ratio = healthy.toFloat() / total
    }
}