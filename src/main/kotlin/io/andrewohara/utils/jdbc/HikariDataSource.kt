package io.andrewohara.utils.jdbc

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.http4k.core.Uri
import org.http4k.core.query
import java.time.Duration
import java.util.concurrent.ThreadFactory
import javax.sql.DataSource

fun hikariDataSource(
    host: Uri,
    username: String,
    password: String,
    minConnections: Int = 1,
    maxConnections: Int = 10,
    idleTimeout: Duration = Duration.ofMinutes(5),
    leakDetectionThreshold: Duration? = null,
    queryParams: Map<String, String> = emptyMap(),
    threadFactory: ThreadFactory? = null
): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = host.apply {
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