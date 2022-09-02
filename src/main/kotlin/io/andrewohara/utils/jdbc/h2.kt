package io.andrewohara.utils.jdbc

import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

class TestDb private constructor(private val dataSource: DataSource): DataSource by dataSource {

    companion object {
        operator fun invoke(dbName: String = "test", mode: String = "MySQL")
            = invoke(dbName, "MODE" to mode, "DB_CLOSE_DELAY" to "-1")

        operator fun invoke(dbName: String, vararg params: Pair<String, String>): TestDb {
            val paramString = params.joinToString(";") { (key, value) -> "$key=$value" }
            val dataSource = JdbcDataSource().apply {
                setURL("jdbc:h2:mem:$dbName;$paramString")
                this.user = "sa"
                this.password = ""
            }
            return TestDb(dataSource)
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