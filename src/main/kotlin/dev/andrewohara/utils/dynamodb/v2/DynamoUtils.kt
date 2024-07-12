package dev.andrewohara.utils.dynamodb.v2

import software.amazon.awssdk.enhanced.dynamodb.*
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

object DynamoLimits {
    const val batchSize = 25
}

class InstantAsEpochSecondConverter: AttributeConverter<Instant> {

    override fun transformFrom(input: Instant): AttributeValue = AttributeValue.builder().n(input.epochSecond.toString()).build()

    override fun transformTo(input: AttributeValue): Instant = Instant.ofEpochSecond(input.n().toLong())

    override fun type(): EnhancedType<Instant> = EnhancedType.of(Instant::class.java)

    override fun attributeValueType(): AttributeValueType = AttributeValueType.N
}

inline fun <reified T> DynamoDbEnhancedClient.batchPut(table: DynamoDbTable<T>, items: Collection<T>, batchSize: Int = DynamoLimits.batchSize) {
    if (items.isEmpty()) return
    require(batchSize <= DynamoLimits.batchSize) { "Batch size must not exceed DynamoDB limit of ${DynamoLimits.batchSize}"}

    val batches = items.chunked(batchSize).map { chunk ->
        WriteBatch.builder(T::class.java).apply {
            mappedTableResource(table)
            for (item in chunk) {
                addPutItem(item)
            }
        }.build()
    }

    for (batch in batches) {
        batchWriteItem {
            it.addWriteBatch(batch)
        }
    }
}

inline fun <reified T> DynamoDbEnhancedClient.batchGet(table: DynamoDbTable<T>, keys: Collection<Key>): List<T> {
    if (keys.isEmpty()) return emptyList()

    val readBatch = keys.map {
        ReadBatch.builder(T::class.java).apply {
            mappedTableResource(table)
            addGetItem(it)
        }.build()
    }

    return readBatch.flatMap { batch ->
        batchGetItem {
            it.addReadBatch(batch)
        }.resultsForTable(table)
    }
}

inline fun <reified T> DynamoDbEnhancedClient.batchDelete(table: DynamoDbTable<T>, keys: Collection<Key>, batchSize: Int = DynamoLimits.batchSize) {
    if (keys.isEmpty()) return
    require(batchSize <= DynamoLimits.batchSize) { "Batch size must not exceed DynamoDB limit of ${DynamoLimits.batchSize}"}

    val batches = keys.chunked(batchSize).map { chunk ->
        WriteBatch.builder(T::class.java).apply {
            mappedTableResource(table)
            for (key in chunk) {
                addDeleteItem(key)
            }
        }.build()
    }

    for (batch in batches) {
        batchWriteItem {
            it.addWriteBatch(batch)
        }
    }
}