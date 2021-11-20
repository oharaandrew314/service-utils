package io.andrewohara.utils.features

import io.split.client.SplitClientConfig
import io.split.client.SplitFactoryBuilder

fun FeatureFlags.Companion.splitIo(apiKey: String) = object: FeatureFlags {

    val client = let {
        val config = SplitClientConfig.builder().build()
        SplitFactoryBuilder.build(apiKey, config).client()
    }

    override fun get(feature: String) =  object: FeatureFlag {
        override fun invoke(key: String) = client.getTreatment(key, feature)
    }
}