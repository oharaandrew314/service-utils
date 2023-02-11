package io.andrewohara.utils.exposed

import dev.forkhandles.values.random
import io.andrewohara.utils.jdbc.TestDb
import io.kotest.matchers.shouldBe
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.UUID

class CatsDaoTest {

    private val db = TestDb(UUID.randomUUID().toString())
        .database()
        .also {
            transaction(it) {
                SchemaUtils.create(CatsTable)
            }
        }

    @Test
    fun `transform nullable columns`() = transaction(db) {
        val owner1 = OwnerId.random()

        val toggles = CatsDao.new {
            name = CatName.of("Toggles")
            ownerId = owner1
        }

        toggles.name shouldBe CatName.of("Toggles")
        toggles.ownerId shouldBe owner1
    }
}