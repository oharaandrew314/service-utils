package io.andrewohara.utils.features

fun interface FeatureFlags {
    operator fun get(feature: String): FeatureFlag

    companion object
}

fun interface FeatureFlag {
    operator fun invoke(key: String): String

    fun isEnabled(key: String): Boolean {
        val state = invoke(key)
        return enabledStates.any { it.equals(state, ignoreCase = true) }
    }

    companion object {
        private val enabledStates = arrayOf("on", "enabled", "true", "yes")
    }
}

fun FeatureFlag.Companion.static(value: String = "on") = static(emptyMap(), value)
fun FeatureFlag.Companion.static(values: Map<String, String>, defaultValue: String = "on") = FeatureFlag { key -> values[key] ?: defaultValue }

fun FeatureFlags.Companion.static(value: String = "on") = static(defaultState = value)
fun FeatureFlags.Companion.static(vararg states: Pair<String, String>, defaultState: String = "on") = static(states.toMap(), defaultState)
fun FeatureFlags.Companion.static(features: Map<String, String>, defaultState: String = "on") = FeatureFlags { name ->
    FeatureFlag.static(features[name] ?: defaultState)
}