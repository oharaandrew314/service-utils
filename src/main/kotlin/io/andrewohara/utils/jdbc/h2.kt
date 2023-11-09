package io.andrewohara.utils.jdbc

import org.h2.jdbcx.JdbcDataSource
import java.net.URI
import javax.sql.DataSource

class TestDb private constructor(
    val uri: URI,
    val dataSource: DataSource
): DataSource by dataSource {

    companion object {
        operator fun invoke(dbName: String = "test", mode: String = "MySQL")
            = invoke(dbName, "MODE" to mode, "DB_CLOSE_DELAY" to "-1")

        operator fun invoke(dbName: String, vararg params: Pair<String, String>): TestDb {
            val paramString = params.joinToString(";") { (key, value) -> "$key=$value" }
            val uri = URI("jdbc:h2:mem:$dbName;$paramString")
            val dataSource = JdbcDataSource().apply {
                setURL(uri.toString())
                this.user = "sa"
                this.password = ""
            }
            return TestDb(uri, dataSource)
        }
    }

    fun execute(sql: String): Boolean {
        dataSource.connection.use { conn ->
            conn.prepareStatement(sql).use { stmt ->
                return stmt.execute()
            }
        }
    }

    fun executeClassResource(resourceName: String): Boolean {
        val sql = javaClass.getResourceAsStream(resourceName)!!.reader().readText()
        return execute(sql)
    }

    fun executeResource(resourceName: String): Boolean {
        val sql = javaClass.classLoader.getResourceAsStream(resourceName)!!.reader().readText()
        return execute(sql)
    }
}