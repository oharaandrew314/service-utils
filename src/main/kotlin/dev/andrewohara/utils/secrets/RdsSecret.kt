package dev.andrewohara.utils.secrets

import java.sql.Connection
import java.sql.DriverManager

data class RdsSecret(
    val username: String,
    val password: String,
    val engine: String,
    val host: String,
    val port: Int,
    val dbname: String,
    val dbInstanceIdentifier: String
) {
    override fun toString() = dbInstanceIdentifier
}

fun RdsSecret.jdbcUri() = "jdbc:$engine://$host/$dbname"

fun RdsSecret.createConnection(): Connection = DriverManager.getConnection(jdbcUri(), username, password)