package dev.andrewohara.utils.features

import software.amazon.awssdk.services.evidently.EvidentlyClient
import software.amazon.awssdk.services.evidently.model.EvaluateFeatureResponse

fun FeatureFlags.Companion.evidently(
    client: EvidentlyClient,
    project: String,
    getValue: (EvaluateFeatureResponse) -> String = { it.variation() }
) = FeatureFlags { feature ->
    FeatureFlag { entity ->
        client.evaluateFeature {
            it.entityId(entity)
            it.project(project)
            it.feature(feature)
        }.let(getValue)
    }
}