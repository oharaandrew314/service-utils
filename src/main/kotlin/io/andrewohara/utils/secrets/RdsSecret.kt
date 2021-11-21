package io.andrewohara.utils.secrets

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