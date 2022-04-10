package io.andrewohara.utils.jdbc

import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

class TestDb private constructor(private val dataSource: DataSource): DataSource by dataSource {

    companion object {
        operator fun invoke(dbName: String = "test", mode: String = "MySQL"): TestDb {
            val dataSource = JdbcDataSource().apply {
                setURL("jdbc:h2:mem:$dbName;MODE=$mode;DB_CLOSE_DELAY=-1")
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