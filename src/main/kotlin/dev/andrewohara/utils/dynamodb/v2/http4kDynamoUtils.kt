package dev.andrewohara.utils.dynamodb.v2

import org.http4k.core.Uri
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class UriConverter: AttributeConverter<Uri> {
    override fun transformFrom(input: Uri): AttributeValue = AttributeValue.builder().s(input.toString()).build()

    override fun transformTo(input: AttributeValue) = Uri.of(input.s())

    override fun type(): EnhancedType<Uri> = EnhancedType.of(Uri::class.java)

    override fun attributeValueType(): AttributeValueType = AttributeValueType.S
}