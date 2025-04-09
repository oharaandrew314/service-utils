package dev.andrewohara.utils.retry

import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapperSchema
import org.http4k.connect.amazon.dynamodb.mapper.plusAssign
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.lens.BiDiMapping
import java.time.Clock
import java.time.Duration
import java.time.Instant

class DynamoRetryStorage<ID: Any>(
    private val table: DynamoDbTableMapper<DynamoRetryLimiterData, String, Unit>,
    private val idMapper: BiDiMapping<ID, String>,
    private val clock: Clock = Clock.systemUTC(),
    private val retention: Duration = Duration.ofDays(1),
): RetryStorage<ID> {

    companion object {
        val schema = DynamoDbTableMapperSchema.Primary<DynamoRetryLimiterData, String, Unit>(
            hashKeyAttribute = Attribute.string().required("id")
        )
    }

    override fun get(id: ID) = table[idMapper(id)]
        ?.let { RetryLimiterData(it.attempts, it.nextAttempt) }

    override fun set(id: ID, data: RetryLimiterData) {
       table += DynamoRetryLimiterData(
           id = idMapper(id),
           attempts = data.attempts,
           nextAttempt = data.nextAttempt,
           expires = clock.instant().plus(retention).epochSecond
       )
    }

    override fun minusAssign(id: ID) = table.delete(idMapper(id))
}

data class DynamoRetryLimiterData(
    val id: String,
    val attempts: Int,
    val nextAttempt: Instant,
    val expires: Long
)



