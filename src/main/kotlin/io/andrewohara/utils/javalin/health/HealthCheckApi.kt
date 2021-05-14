package io.andrewohara.utils.javalin.health

import io.javalin.Javalin
import io.javalin.core.security.Role
import io.javalin.core.security.SecurityUtil
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiContent
import io.javalin.plugin.openapi.annotations.OpenApiResponse

class HealthCheckApi(app: Javalin, private val handler: HealthCheckHandler, roles: Set<Role> = emptySet()) {

    init {
        app.get("/health", ::healthCheckV2, SecurityUtil.roles(*roles.toTypedArray()))
    }

    @OpenApi(
        summary = "Return health status",
        operationId = "healthCheck",
        responses = [
            OpenApiResponse("200", [OpenApiContent(HealthCheckDtoV1::class)], "healthy"),
            OpenApiResponse("503", [OpenApiContent(HealthCheckDtoV1::class)], "unhealthy")
        ]
    )
    private fun healthCheckV2(context: Context) {
        val health = handler.check()

        val response = HealthCheckDtoV1(
            results = health.results,
            healthy = health.healthy,
            unhealthy = health.unhealthy,
            total = health.total,
            ratio = health.ratio
        )

        context.status(if (health.unhealthy > 0) 503 else 200)
        context.json(response)
    }

    data class HealthCheckDtoV1(
        val results: Map<String, Boolean>,
        val healthy: Int,
        val unhealthy: Int,
        val total: Int,
        val ratio: Float
    )
}