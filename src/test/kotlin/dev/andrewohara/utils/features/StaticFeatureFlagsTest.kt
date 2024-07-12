package dev.andrewohara.utils.features

import dev.andrewohara.utils.features.FeatureFlags
import dev.andrewohara.utils.features.static

class StaticFeatureFlagsTest: AbstractFeatureFlagsTest() {
    override fun getFeatureFlags(states: Map<String, String>, defaultState: String) = FeatureFlags.static(states, defaultState)
}