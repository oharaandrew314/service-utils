package io.andrewohara.utils.features

import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import org.http4k.connect.amazon.evidently.FakeEvidently
import org.http4k.connect.amazon.evidently.actions.VariableValue
import org.http4k.connect.amazon.evidently.createFeature
import org.http4k.connect.amazon.evidently.createProject
import org.http4k.connect.amazon.evidently.model.EntityId
import org.http4k.connect.amazon.evidently.model.FeatureName
import org.http4k.connect.amazon.evidently.model.ProjectName
import org.http4k.connect.amazon.evidently.model.VariationName

fun FakeEvidently.testFeature(): Pair<ProjectName, FeatureName> {
    val project = client().createProject(ProjectName.of(javaClass.simpleName))
        .onFailure { it.reason.throwIt() }
        .project

    val feature = client().createFeature(
        project = project.name,
        name = FeatureName.of("feature"),
        defaultVariation = VariationName.of("legacy"),
        variations = mapOf(
            VariationName.of("legacy") to VariableValue("java"),
            VariationName.of("modern") to VariableValue("kotlin")
        ),
        entityOverrides = mapOf(
            EntityId.of("andrew") to VariationName.of("modern")
        )
    )
        .map { it.feature }
        .onFailure { it.reason.throwIt() }

    return project.name to feature.name
}