package dev.andrewohara.utils.retry

import java.time.Instant

interface RetryStorage<ID: Any> {
    operator fun get(id: ID): RetryLimiterData?
    operator fun set(id: ID, data: RetryLimiterData)
    operator fun minusAssign(id: ID)
}

data class RetryLimiterData(
    val attempts: Int,
    val nextAttempt: Instant
)