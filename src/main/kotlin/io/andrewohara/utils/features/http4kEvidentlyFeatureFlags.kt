package io.andrewohara.utils.features

import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import org.http4k.connect.amazon.evidently.Evidently
import org.http4k.connect.amazon.evidently.evaluateFeature
import org.http4k.connect.amazon.evidently.model.EntityId
import org.http4k.connect.amazon.evidently.model.FeatureName
import org.http4k.connect.amazon.evidently.model.ProjectName

fun FeatureFlags.Companion.evidently(client: Evidently, project: ProjectName) = FeatureFlags { feature ->
    FeatureFlag { entity ->
        client.evaluateFeature(
            project = project,
            feature = FeatureName.of(feature),
            entityId = EntityId.of(entity)
        )
            .map { it.value.stringValue!! }
            .onFailure { it.reason.throwIt() }
    }
}