package dev.andrewohara.utils.features

import io.andrewohara.utils.features.FeatureFlags
import io.andrewohara.utils.features.static

class StaticFeatureFlagsTest: AbstractFeatureFlagsTest() {
    override fun getFeatureFlags(states: Map<String, String>, defaultState: String) = FeatureFlags.static(states, defaultState)
}