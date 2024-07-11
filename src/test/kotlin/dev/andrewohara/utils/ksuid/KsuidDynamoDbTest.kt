package dev.andrewohara.utils.ksuid

import io.kotest.matchers.shouldBe
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.AttributeValue
import org.junit.jupiter.api.Test

class KsuidDynamoDbTest {

    private val attr = Attribute.value(MyKsuidValue).required("id")

    @Test
    fun `convert Ksuid to AttributeValue`() {
        attr.asValue(MyKsuidValue.parse("2XwxFV1hgeQ2QFidgkCH9BoTwPQ")) shouldBe AttributeValue.Str("2XwxFV1hgeQ2QFidgkCH9BoTwPQ")
    }
}