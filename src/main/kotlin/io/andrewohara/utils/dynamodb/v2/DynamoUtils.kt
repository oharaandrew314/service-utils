package io.andrewohara.utils.dynamodb.v2

import software.amazon.awssdk.enhanced.dynamodb.*
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

inline fun <reified T> DynamoDbTable<T>.batchPut(client: DynamoDbEnhancedClient, items: Collection<T>, batchSize: Int = DynamoLimits.batchSize) {
    if (items.isEmpty()) return
    require(batchSize <= DynamoLimits.batchSize) { "Batch size must not exceed DynamoDB limit of ${DynamoLimits.batchSize}"}

    val batches = items.chunked(batchSize).map { chunk ->
        WriteBatch.builder(T::class.java).apply {
            mappedTableResource(this@batchPut)
            for (item in chunk) {
                addPutItem(item)
            }
        }.build()
    }

    for (batch in batches) {
        client.batchWriteItem {
            it.addWriteBatch(batch)
        }
    }
}