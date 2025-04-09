package dev.andrewohara.utils.retry

import com.github.fppt.jedismock.RedisServer
import dev.andrewohara.utils.jdk.toClock
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.http4k.format.Moshi
import org.http4k.lens.BiDiMapping
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.clients.jedis.JedisPool
import java.time.Duration
import java.time.Instant

private typealias Task = (Int) -> Result4k<Int, Int>

private val succeed: Task = { Success(it) }
private val fail: Task = { Failure(it) }

abstract class RetryLimiterContract() {

    private val clock = Instant.parse("2025-04-14T12:00:00Z").toClock()

    abstract fun getStorage(): RetryStorage<String>

    private lateinit var retries: RetryLimiter<String>

    @BeforeEach
    fun setup() {
        retries = RetryLimiter(
            storage = getStorage(),
            clock = clock,
            intervalFunction = { Duration.ofSeconds(it * 2L) }
        )
    }

    @Test
    fun `succeed on first attempt`() {
        retries.attempt("id1", succeed) shouldBeSuccess 1
    }

    @Test
    fun `next attempt after failure is throttled`() {
        retries.attempt("id1", fail) shouldBeFailure RetryError.TaskError(
            error = 1,
            nextAttempt = clock.instant() + Duration.ofSeconds(2)
        )

        retries.attempt("id1", succeed) shouldBeFailure RetryError.Throttled(
            nextAttempt = clock.instant().plusSeconds(2)
        )
    }

    @Test
    fun `next attempt scales with interval function`() {
        retries.attempt("id1", fail) shouldBeFailure RetryError.TaskError(
            error = 1,
            nextAttempt = clock.instant() + Duration.ofSeconds(2)
        )

        clock += Duration.ofSeconds(3)
        retries.attempt("id1", fail) shouldBeFailure RetryError.TaskError(
            error = 2,
            nextAttempt = clock.instant() + Duration.ofSeconds(4)
        )

        clock += Duration.ofSeconds(3)
        retries.attempt("id1", succeed) shouldBeFailure RetryError.Throttled(
            nextAttempt = clock.instant() + Duration.ofSeconds(1)
        )
    }

    @Test
    fun `wait for throttle to expire`() {
        retries.attempt("id1", fail) shouldBeFailure RetryError.TaskError(
            error = 1,
            nextAttempt = clock.instant() + Duration.ofSeconds(2)
        )

        retries.attempt("id1", succeed) shouldBeFailure RetryError.Throttled(
            nextAttempt = clock.instant().plusSeconds(2)
        )

        clock += Duration.ofSeconds(3)
        // previous attempt doesn't count, because it was throttled
        retries.attempt("id1", succeed) shouldBeSuccess 2
    }

    @Test
    fun `attempts don't affect other keys`() {
        retries.attempt("id1", fail) shouldBeFailure RetryError.TaskError(
            error = 1,
            nextAttempt = clock.instant() + Duration.ofSeconds(2)
        )

        retries.attempt("id2", succeed) shouldBeSuccess 1
    }
}

class DynamoRetryLimiterTest: RetryLimiterContract() {
    override fun getStorage() = DynamoRetryStorage(
        table = FakeDynamoDb().client()
            .tableMapper(TableName.of("retries"), DynamoRetryStorage.schema)
            .also { it.createTable().shouldBeSuccess() },
        idMapper = BiDiMapping(String::toString, String::toString),
    )
}

class MemoryRetryLimiterTest: RetryLimiterContract() {
    override fun getStorage() = MemoryRetryStorage<String>()
}

class JedisRetryLimiterTest: RetryLimiterContract() {

    override fun getStorage(): RetryStorage<String> {
        val fake = RedisServer.newRedisServer().start()
        val pool = JedisPool(fake.host, fake.bindPort)
        return JedisRetryStorage(pool, BiDiMapping(String::toString, String::toString), Moshi)
    }
}