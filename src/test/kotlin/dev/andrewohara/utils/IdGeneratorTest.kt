package dev.andrewohara.utils

import io.andrewohara.utils.IdGenerator
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import org.junit.jupiter.api.Test
import java.util.Random

class IdGeneratorTest {

    private val testObj = IdGenerator(Random(1337))

    @Test
    fun `next hex`() {
        testObj.nextHex(8) shouldBe "19E79D84"
    }

    @Test
    fun `next base36`() {
        testObj.nextBase36(8) shouldBe "QETRJ88E"
    }

    @Test
    fun `companion object`() {
        IdGenerator.nextHex(4).shouldHaveLength(4)
        IdGenerator.nextBase36(4).shouldHaveLength(4)
    }
}