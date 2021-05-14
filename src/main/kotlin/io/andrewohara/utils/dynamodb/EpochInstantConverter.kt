package io.andrewohara.utils.dynamodb

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.Instant

class EpochInstantConverter: DynamoDBTypeConverter<Long, Instant> {
    override fun convert(value: Instant) = value.epochSecond
    override fun unconvert(value: Long): Instant = Instant.ofEpochSecond(value)
}