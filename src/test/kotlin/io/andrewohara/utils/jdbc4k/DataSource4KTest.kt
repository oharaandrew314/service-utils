package io.andrewohara.utils.jdbc4k

import io.andrewohara.utils.jdbc.TestDb
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.*

class DataSource4KTest {

    private val dataSource = TestDb(UUID.randomUUID().toString()).dataSource.forK()

    @Test
    fun `round trip`() {
        dataSource.connection { conn ->
            conn.statement { stmt ->
                stmt.execute("CREATE TABLE people (id INTEGER PRIMARY KEY NOT NULL AUTO_INCREMENT, name VARCHAR NOT NULL)")
            }

            val id1 = conn.preparedStatementWithKeys("INSERT INTO people (name) VALUES (:name)") { stmt ->
                stmt["name"] = "josh"

                stmt.executeUpdate { it.getInt("id") }
            }

            conn.preparedStatement("INSERT INTO people (name) VALUES (:name)") { stmt ->
                stmt.batch("john", "jame", "jim") {
                    it["name"] = this
                }
            }

            conn.preparedStatement("SELECT * FROM people WHERE id = :id") { stmt ->
                stmt["id"] = id1

                stmt.executeQuery()
                    .map { it.getString("name") }
                    .toList()
                    .shouldContainExactly("josh")
            }

            conn.preparedStatement("DELETE FROM people WHERE name = :name") { stmt ->
                stmt["name"] = "john"

                stmt.executeUpdate() shouldBe 1
            }
        }
    }
}