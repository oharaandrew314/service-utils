package io.andrewohara.utils.features

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class AbstractFeatureFlagsTest {

    private val defaultState = "lolcats"
    private val states = mapOf(
        "featureA" to "on",
        "featureB" to "disabled"
    )

    private lateinit var flags: FeatureFlags

    abstract fun getFeatureFlags(states: Map<String, String>, defaultState: String): FeatureFlags

    @BeforeEach
    fun setup() {
        flags = getFeatureFlags(states, defaultState)
    }

    @Test
    fun `get state`() {
        flags["featureA"]("userA") shouldBe "on"
    }

    @Test
    fun `get state for missing feature`() {
        flags["featureC"]("userA") shouldBe "lolcats"
    }
}

