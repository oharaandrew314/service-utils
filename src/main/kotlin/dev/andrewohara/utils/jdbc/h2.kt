package dev.andrewohara.utils.jdbc

import org.h2.jdbcx.JdbcDataSource
import java.net.URI

class H2DataSourceBuilder private constructor(
    private val host: String,
    private val params: MutableList<Pair<String, String>> = mutableListOf()
) {
    companion object {
        fun memory(name: String? = null) = H2DataSourceBuilder(if (name == null) "jdbc:h2:mem" else "jdbc:h2:mem:$name")
    }

    fun param(key: String, value: String) = apply { params.add(key to value) }
    fun mysql() = param("MODE", "MySQL")
    fun postgres() = param("MODE", "PostgreSQL")
    fun keepAlive() = param("DB_CLOSE_DELAY", "-1")

    fun build() = JdbcDataSource().apply {
        val uri = URI("$host;${params.joinToString(";") { (key, value) -> "$key=$value" }}")
        setURL(uri.toString())
        this.user = "sa"
        this.password = ""
    }
}