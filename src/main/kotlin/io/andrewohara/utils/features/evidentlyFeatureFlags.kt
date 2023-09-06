package io.andrewohara.utils.features

import software.amazon.awssdk.services.evidently.EvidentlyClient

fun FeatureFlags.Companion.evidently(client: EvidentlyClient, project: String) = FeatureFlags { feature ->
    FeatureFlag { entity ->
        client.evaluateFeature {
            it.entityId(entity)
            it.project(project)
            it.feature(feature)
        }.value().stringValue()
    }
}