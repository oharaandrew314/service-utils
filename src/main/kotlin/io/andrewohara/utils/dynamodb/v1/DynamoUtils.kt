package io.andrewohara.utils.dynamodb.v1

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTableMapper

object DynamoUtils {

    fun <T, H, R> mapper(client: AmazonDynamoDB, tableName: String, dataType: Class<T>): DynamoDBTableMapper<T, H, R> {
        val config = DynamoDBMapperConfig.Builder()
            .withTableNameOverride(DynamoDBMapperConfig.TableNameOverride.withTableNameReplacement(tableName))
            .build()

        return DynamoDBMapper(client, config).newTableMapper(dataType)
    }
}