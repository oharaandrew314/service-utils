package io.andrewohara.utils.features

import io.split.client.SplitClient
import io.split.client.SplitClientConfig
import io.split.client.SplitFactoryBuilder

fun FeatureFlags.Companion.splitIo(apiKey: String) = SplitClientConfig
    .builder().build()
    .let { config -> SplitFactoryBuilder.build(apiKey, config).client() }
    .let { FeatureFlags.splitIo(it) }

fun FeatureFlags.Companion.splitIo(client: SplitClient) = FeatureFlags { feature ->
    FeatureFlag { key -> client.getTreatment(key, feature) }
}