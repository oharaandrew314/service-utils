package io.andrewohara.utils.javalin.health.checks

interface HealthCheck {
    fun isHealthy(): Boolean
}