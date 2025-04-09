package dev.andrewohara.utils.retry

import dev.forkhandles.result4k.*
import java.time.Clock
import java.time.Duration
import java.time.Instant

class RetryLimiter<ID: Any>(
    private val storage: RetryStorage<ID>,
    private val clock: Clock = Clock.systemUTC(),
    private val intervalFunction: (attempt: Int) -> Duration
) {
    fun <Result: Any> attempt(
        id: ID,
        fn: (attempt: Int) -> Result4k<Result, out Any>
    ): Result4k<Result, RetryError> {
        val time = clock.instant()
        val data = storage[id]
        val currentAttempt = (data?.attempts ?: 0) + 1

        if (data != null && time < data.nextAttempt) {
            return Failure(RetryError.Throttled(data.nextAttempt))
        }

        val nextAttempt = time + intervalFunction(currentAttempt)
        return try {
            fn(currentAttempt).mapFailure { RetryError.TaskError(it, nextAttempt) }
        } catch (e: Throwable) {
            Failure(RetryError.TaskError(e, nextAttempt))
        }
            .peek { storage -= id }
            .peekFailure { storage[id] = RetryLimiterData(currentAttempt, nextAttempt) }
    }
}

sealed interface RetryError {
    val nextAttempt: Instant

    data class Throttled(
        override val nextAttempt: Instant
    ): RetryError

    data class TaskError(
        val error: Any,
        override val nextAttempt: Instant
    ): RetryError
}