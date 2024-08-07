package dev.andrewohara.utils.features

import dev.andrewohara.utils.features.FeatureFlag
import dev.andrewohara.utils.features.static
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class StaticFeatureFlagTest {

    private val flag = FeatureFlag.static(
        mapOf("userA" to "on", "userB" to "off"),
        defaultValue = "lolcats"
    )

    @Test
    fun `get for key`() {
        flag("userA") shouldBe "on"
    }

    @Test
    fun `get for missing key`() {
        flag("userC") shouldBe "lolcats"
    }

    @Test
    fun `get enabled for enabled key`() {
        flag.isEnabled("userA") shouldBe true
    }

    @Test
    fun `get enabled for disabled key`() {
        flag.isEnabled("userB") shouldBe false
    }
    
    @Test
    fun `get enabled for missing key`() {
        flag.isEnabled("userC") shouldBe false
    }
}