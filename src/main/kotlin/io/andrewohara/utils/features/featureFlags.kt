package io.andrewohara.utils.features

import java.util.*

interface FeatureFlags {
    operator fun get(feature: String): FeatureFlag

    companion object
}

interface FeatureFlag {
    operator fun invoke(key: String = UUID.randomUUID().toString()): String

    fun isEnabled(key: String = UUID.randomUUID().toString()): Boolean {
        val state = invoke(key)
        return enabledStates.any { it.equals(state, ignoreCase = true) }
    }

    companion object {
        private val enabledStates = arrayOf("on", "enabled", "true", "yes")
    }
}

fun FeatureFlag.Companion.static(value: String = "on") = object: FeatureFlag {
    override fun invoke(key: String) = value
}

fun FeatureFlags.Companion.static(value: String = "on") = object: FeatureFlags {
    override fun get(feature: String) = FeatureFlag.static(value)
}