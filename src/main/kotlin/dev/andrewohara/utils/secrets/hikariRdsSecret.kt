package io.andrewohara.utils.secrets

import java.time.Duration
import javax.sql.DataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4k.core.Uri
import org.http4k.core.query
import java.util.concurrent.ThreadFactory

fun RdsSecret.hikariDataSource(
    minConnections: Int = 1,
    maxConnections: Int = 10,
    idleTimeout: Duration = Duration.ofMinutes(5),
    leakDetectionThreshold: Duration? = null,
    queryParams: Map<String, String> = emptyMap(),
    threadFactory: ThreadFactory? = null
): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = Uri.of(jdbcUri()).apply {
        for ((key, value) in queryParams) {
            query(key, value)
        }
    }.toString()
    config.username = username
    config.password = password
    config.minimumIdle = minConnections
    config.maximumPoolSize = maxConnections
    config.idleTimeout = idleTimeout.toMillis()
    config.threadFactory = threadFactory

    if (leakDetectionThreshold != null) {
        config.leakDetectionThreshold = leakDetectionThreshold.toMillis()
    }

    return HikariDataSource(config)
}