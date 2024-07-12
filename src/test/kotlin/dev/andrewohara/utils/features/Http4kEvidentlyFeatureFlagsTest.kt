package dev.andrewohara.utils.features

import dev.andrewohara.utils.features.FeatureFlags
import dev.andrewohara.utils.features.evidently
import io.kotest.matchers.shouldBe
import org.http4k.connect.amazon.evidently.FakeEvidently
import org.http4k.connect.amazon.evidently.model.FeatureName
import org.http4k.connect.amazon.evidently.model.ProjectName
import org.junit.jupiter.api.Test

class Http4kEvidentlyFeatureFlagsTest {

    private val http = FakeEvidently()
    private val projectName: ProjectName
    private val featureName: FeatureName

    init {
        val result = http.testFeature()
        projectName = result.first
        featureName = result.second
    }

    private val features = let {
        FeatureFlags.evidently(http.client(), projectName)
    }

    @Test
    fun `get variation override`() {
        features[featureName.value]("andrew") shouldBe "kotlin"
    }

    @Test
    fun `get default variation`() {
        features[featureName.value]("dinosaur") shouldBe "java"
    }
}