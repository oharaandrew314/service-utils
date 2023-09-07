package io.andrewohara.utils.features

import io.kotest.matchers.shouldBe
import org.http4k.aws.AwsSdkClient
import org.http4k.connect.amazon.evidently.Evidently
import org.http4k.connect.amazon.evidently.FakeEvidently
import org.http4k.connect.amazon.evidently.model.FeatureName
import org.http4k.connect.amazon.evidently.model.ProjectName
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.evidently.EvidentlyClient

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