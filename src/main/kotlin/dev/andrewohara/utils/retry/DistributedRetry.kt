package dev.andrewohara.utils.retry

import dev.forkhandles.result4k.*
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapper
import org.http4k.connect.amazon.dynamodb.mapper.DynamoDbTableMapperSchema
import org.http4k.connect.amazon.dynamodb.mapper.plusAssign
import org.http4k.connect.amazon.dynamodb.model.Attribute
import java.time.Clock
import java.time.Duration
import java.time.Instant

class DistributedRetry<ID: Any>(
    private val table: DynamoDbTableMapper<DistributedRetryData, String, Unit>,
    private val clock: Clock,
    private val intervalFunction: (attempt: Int) -> Duration,
    private val idToString: (ID) -> String = { it.toString() },
    private val retention: Duration = Duration.ofDays(1),
) {
    companion object {
        val schema = DynamoDbTableMapperSchema.Primary<DistributedRetryData, String, Unit>(
            hashKeyAttribute = Attribute.string().required("id")
        )
    }

    fun <Result: Any, Error: Any> attempt(
        id: ID,
        fn: (attempt: Int) -> Result4k<Result, Error>
    ): Result4k<Result, RetryError> {
        val idString = idToString(id)
        val time = clock.instant()
        val data = table[idString]
        val attempt = (data?.attempts ?: 0) + 1

        if (data != null) {
            val nextAttempt = data.lastFailure + intervalFunction(attempt)
            if (time >= nextAttempt) return Failure(RetryError.Throttled(attempt, nextAttempt))
        }

        return try {
            fn(attempt).mapFailure { RetryError.TaskError(it) }
        } catch (e: Throwable) {
            Failure(RetryError.Error(e))
        }
            .peek { table.delete(idString) }
            .peekFailure {
                table += DistributedRetryData(
                    id = idString,
                    attempts = attempt,
                    lastFailure = time,
                    expires = time.plus(retention).epochSecond
                )
            }
    }
}

data class DistributedRetryData(
    val id: String,
    val attempts: Int,
    val lastFailure: Instant,
    val expires: Long
)

sealed interface RetryError {
    data class Throttled(val attempt: Int, val nextAttempt: Instant): RetryError
    data class Error(val error: Throwable): RetryError
    data class TaskError<Error: Any>(val error: Error): RetryError
}