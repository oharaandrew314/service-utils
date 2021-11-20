package io.andrewohara.utils.dynamodb.v1

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTableMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import java.time.Duration
import java.time.Instant

fun <T, H, R> AmazonDynamoDB.mapper(tableName: String, dataType: Class<T>): DynamoDBTableMapper<T, H, R> {
    val config = DynamoDBMapperConfig.Builder()
        .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
        .build()

    return DynamoDBMapper(this, config).newTableMapper(dataType)
}

inline fun <reified T, H, R> AmazonDynamoDB.mapper(tableName: String): DynamoDBTableMapper<T, H, R> = mapper(tableName, T::class.java)

class UnixInstantDynamoDBTypeConverter: DynamoDBTypeConverter<Long, Instant> {
    override fun convert(value: Instant) = value.epochSecond
    override fun unconvert(value: Long): Instant = Instant.ofEpochSecond(value)
}

class IsoInstantDynamoDBTypeConverter: DynamoDBTypeConverter<String, Instant> {
    override fun convert(value: Instant) = value.toString()
    override fun unconvert(value: String): Instant = Instant.parse(value)
}

class IsoDurationDynamoDBTypeConverter: DynamoDBTypeConverter<String, Duration> {
    override fun convert(duration: Duration) = duration.toString()
    override fun unconvert(value: String): Duration = Duration.parse(value)
}