package io.andrewohara.utils.dynamodb.v1

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTableMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import io.andrewohara.awsmock.dynamodb.MockDynamoDbV1
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class DynamoDbTest {

    @DynamoDBDocument
    data class Cat(
        @DynamoDBHashKey
        var name: String? = null,

        @DynamoDBTypeConverted(converter = IsoInstantDynamoDBTypeConverter::class)
        var birthDate: Instant? = null,

        @DynamoDBTypeConverted(converter = UnixInstantDynamoDBTypeConverter::class)
        var adoptionDate: Instant? = null,

        @DynamoDBTypeConverted(converter = IsoDurationDynamoDBTypeConverter::class)
        var meowLength: Duration? = null
    )

    private val mapper: DynamoDBTableMapper<Cat, String, Unit> = MockDynamoDbV1()
        .mapper<Cat, String, Unit>("cats")
        .also { it.createTable(ProvisionedThroughput(1, 1)) }

    @Test
    fun `save and get fully detailed`() {
        val cat = Cat(
            name = "Toggles",
            birthDate = Instant.ofEpochSecond(1337),
            adoptionDate = Instant.ofEpochSecond(9001),
            meowLength = Duration.ofHours(3)
        ).also { mapper.save(it) }

        mapper.load("Toggles") shouldBe cat
    }

    @Test
    fun `save and get minimal`() {
        val cat = Cat(name = "Toggles").also {
            mapper.save(it)
        }

        mapper.load("Toggles") shouldBe cat
    }
}