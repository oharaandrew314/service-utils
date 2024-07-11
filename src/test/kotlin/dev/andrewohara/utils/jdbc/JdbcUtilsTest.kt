package dev.andrewohara.utils.jdbc

import io.kotest.matchers.sequences.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class JdbcUtilsTest {

    companion object {
        val createCat = JdbcUtilsTest::class.java.getResourceAsStream("create-cat.sql")!!.reader().readText()
        val getCat = JdbcUtilsTest::class.java.getResourceAsStream("get-cat.sql")!!.reader().readText()
    }

    data class Cat(
        val id: Int,
        val name: String?,
        val lives: Int?,
        val born: Instant?,
        val trills: Boolean?
    )

    private val dataSource = TestDb()

    @BeforeEach
    fun createTables() {
        dataSource.executeClassResource("create-tables.sql")
    }

    @AfterEach
    fun truncateTables() {
        dataSource.executeClassResource("truncate-tables.sql")
    }

    @Test
    fun `ResultSet toSequence`() {
        createCat("Toggles", 1, Instant.parse("2004-06-01T03:33:00Z"), true)
        createCat("Bandit", 8, Instant.parse("2016-07-01T12:15:00Z"), false)

        dataSource.connection.use { conn ->
            conn.prepareStatement("SELECT * FROM cats").use { stmt ->
                stmt.executeQuery().use { rs ->
                    rs.toSequence()
                        .map { row -> row.getString("name") }
                        .shouldContainExactly("Toggles", "Bandit")
                }
            }
        }
    }

    @Test
    fun `create empty cat`() {
        val created = createCat(null, null, null, null)
        getCat(created.id) shouldBe created
    }

    @Test
    fun `create cat`() {
        val created = createCat("Toggles", 1, Instant.parse("2004-06-01T03:33:00Z"), true)
        getCat(created.id) shouldBe created
    }

    private fun createCat(name: String?, lives: Int?, born: Instant?, trills: Boolean?): Cat {
        val id = dataSource.connection.use { conn ->
            conn.prepareStatement(createCat, arrayOf("id")).use { stmt ->
                stmt.setNullableString(1, name)
                stmt.setNullableInt(2, lives)
                stmt.setNullableInstant(3, born)
                stmt.setNullableBoolean(4, trills)

                require(stmt.executeUpdate() == 1)
                stmt.generatedKeys.use { rs ->
                    require(rs.next())
                    rs.getInt("id")
                }
            }
        }

        return Cat(
            id = id,
            name = name,
            lives = lives,
            born = born,
            trills = trills
        )
    }

    private fun getCat(id: Int): Cat? {
        dataSource.connection.use { conn ->
            conn.prepareStatement(getCat).use { stmt ->
                stmt.setInt(1, id)

                stmt.executeQuery().use { rs ->
                    if (!rs.next()) return null

                    return Cat(
                        id = rs.getInt("id"),
                        name = rs.getString("name"),
                        lives = rs.getIntOrNull("lives"),
                        born = rs.getInstantOrNull("born"),
                        trills = rs.getBoolOrNull("trills")
                    )
                }
            }
        }
    }
}