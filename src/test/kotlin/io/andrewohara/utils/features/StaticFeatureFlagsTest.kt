package io.andrewohara.utils.features

class StaticFeatureFlagsTest: AbstractFeatureFlagsTest() {
    override fun getFeatureFlags(states: Map<String, String>, defaultState: String) = FeatureFlags.static(states, defaultState)
}