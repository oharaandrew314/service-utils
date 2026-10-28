package dev.andrewohara.utils

import dev.andrewohara.utils.jdk.nextBase36
import dev.andrewohara.utils.jdk.nextHex
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import org.junit.jupiter.api.Test
import java.util.Random

class RandomExtensionsTest {

    private val random = Random(1337)

    @Test
    fun `next hex`() {
        random.nextHex(8) shouldBe "19E79D84"
    }

    @Test
    fun `next base36`() {
        random.nextBase36(8) shouldBe "QETRJ88E"
    }

    @Test
    fun `companion object`() {
        random.nextHex(4).shouldHaveLength(4)
        random.nextBase36(4).shouldHaveLength(4)
    }
}