package dev.andrewohara.utils.features

import io.andrewohara.utils.features.FeatureFlags
import io.andrewohara.utils.features.evidently
import io.kotest.matchers.shouldBe
import org.http4k.aws.AwsSdkClient
import org.http4k.connect.amazon.evidently.FakeEvidently
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.evidently.EvidentlyClient

class OfficialEvidentlyFeatureFlagsTest {

    private val http = FakeEvidently()
    private val projectName: String
    private val featureName: String

    init {
        val result = http.testFeature()
        projectName = result.first.value
        featureName = result.second.value
    }

    private val features = let {
        val sdk = EvidentlyClient.builder()
            .httpClient(AwsSdkClient(http))
            .credentialsProvider { AwsBasicCredentials.create("id", "secret") }
            .region(Region.CA_CENTRAL_1)
            .build()

        FeatureFlags.evidently(sdk, projectName)
    }

    @Test
    fun `get variation override`() {
        features[featureName]("andrew") shouldBe "kotlin"
    }

    @Test
    fun `get default variation`() {
        features[featureName]("dinosaur") shouldBe "java"
    }
}