package io.andrewohara.utils.dynamodb.v1

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.Duration

class IsoDurationConverter: DynamoDBTypeConverter<String, Duration> {
    override fun convert(duration: Duration) = duration.toString()
    override fun unconvert(value: String): Duration = Duration.parse(value)
}