package io.andrewohara.utils.dynamodb.v2

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.http4k.aws.AwsSdkClient
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.mapper.BeanTableSchema
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

@DynamoDbBean
data class DynamoV2Cat(
    @get:DynamoDbPartitionKey var id: Int? = null,
    var name: String? = null,

    @get:DynamoDbConvertedBy(InstantAsEpochSecondConverter::class)
    var dob: Instant? = null,

    @get:DynamoDbConvertedBy(UriConverter::class)
    var photo: Uri? = null
)

class DynamoV2Test {
    private val dynamo = DynamoDbClient.builder()
        .httpClient(AwsSdkClient(FakeDynamoDb()))
        .credentialsProvider { AwsBasicCredentials.create("id", "secret") }
        .region(Region.CA_CENTRAL_1)
        .build()

    private val enhanced = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(dynamo)
        .build()

    private val table = enhanced.table("cats", BeanTableSchema.create(DynamoV2Cat::class.java))
        .also { it.createTable() }

    @Test
    fun `batch put`() {
        val kitties = (1..100).map { DynamoV2Cat(it, "cat$it") }

        enhanced.batchPut(table, kitties)

        table.scan().items().toList().shouldContainExactlyInAnyOrder(kitties)
    }

    @Test
    fun `batch get`() {
        val kitties = (1..100).map { DynamoV2Cat(it, "cat$it") }
        kitties.forEach { table.putItem(it) }

        val keys = kitties.map { Key.builder().partitionValue(it.id).build() }

        enhanced.batchGet(table, keys).shouldContainExactlyInAnyOrder(kitties)
    }

    @Test
    fun `batch delete`() {
        val kitties = (1..100).map { DynamoV2Cat(it, "cat$it") }
        kitties.forEach { table.putItem(it) }

        enhanced.batchDelete(table, kitties.map { Key.builder().partitionValue(it.id).build() })

        table.scan().items().shouldBeEmpty()
    }

    @Test
    fun `convert instant as epoch second`() {
        val cat = DynamoV2Cat(1, dob = Instant.parse("2022-09-04T12:30:00Z"))
        table.putItem(cat)
        table.getItem(Key.builder().partitionValue(1).build()) shouldBe cat

        dynamo.getItem {
            it.tableName("cats")
            it.key(mapOf("id" to AttributeValue.builder().n("1").build()))
        }.item() shouldBe mapOf(
            "id" to AttributeValue.builder().n("1").build(),
            "dob" to AttributeValue.builder().n(cat.dob!!.epochSecond.toString()).build()
        )
    }

    @Test
    fun `convert http4k uri`() {
        val uri = Uri.of("https://cats.com/photo1.jpg")
        val cat = DynamoV2Cat(1, photo = uri)

        table.putItem(cat)
        table.getItem(Key.builder().partitionValue(1).build()) shouldBe cat

        dynamo.getItem {
            it.tableName("cats")
            it.key(mapOf("id" to AttributeValue.builder().n("1").build()))
        }.item() shouldBe mapOf(
            "id" to AttributeValue.builder().n("1").build(),
            "photo" to AttributeValue.builder().s(uri.toString()).build()
        )
    }
}