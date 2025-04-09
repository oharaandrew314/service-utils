package dev.andrewohara.utils.retry

import com.github.benmanes.caffeine.cache.Caffeine
import java.time.Duration

class MemoryRetryStorage<ID: Any>(
    retention: Duration = Duration.ofDays(1)
): RetryStorage<ID> {

    private val retries = Caffeine.newBuilder()
        .expireAfterWrite(retention)
        .build<ID, RetryLimiterData>()

    override fun get(id: ID) = retries.getIfPresent(id)

    override fun set(id: ID, data: RetryLimiterData) = retries.put(id, data)

    override fun minusAssign(id: ID) = retries.invalidate(id)
}