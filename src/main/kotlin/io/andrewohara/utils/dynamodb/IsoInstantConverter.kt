package io.andrewohara.utils.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.Instant

class IsoInstantConverter: DynamoDBTypeConverter<String, Instant> {
    override fun convert(value: Instant) = value.toString()
    override fun unconvert(value: String): Instant = Instant.parse(value)
}