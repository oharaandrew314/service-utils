package io.andrewohara.utils.secrets

import java.time.Duration
import javax.sql.DataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun RdsSecret.hikariDataSource(
    minConnections: Int = 1,
    maxConnections: Int = 10,
    idleTimeout: Duration = Duration.ofMinutes(5),
    leakDetectionThreshold: Duration? = null
): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = jdbcUri()
    config.username = username
    config.password = password
    config.minimumIdle = minConnections
    config.maximumPoolSize = maxConnections
    config.idleTimeout = idleTimeout.toMillis()

    if (leakDetectionThreshold != null) {
        config.leakDetectionThreshold = leakDetectionThreshold.toMillis()
    }

    return HikariDataSource(config)
}