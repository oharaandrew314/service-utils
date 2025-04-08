package dev.andrewohara.utils.retry

import dev.andrewohara.utils.jdk.toClock
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import io.kotest.matchers.shouldBe
import org.http4k.connect.amazon.dynamodb.FakeDynamoDb
import org.http4k.connect.amazon.dynamodb.mapper.tableMapper
import org.http4k.connect.amazon.dynamodb.model.TableName
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class DistributedRetryTest {

    private val clock = Instant.parse("2025-04-14T12:00:00Z").toClock()

    private val retries = DistributedRetry<String>(
        table = FakeDynamoDb().client().tableMapper(TableName.of("retries"), DistributedRetry.schema)
            .also { it.createTable().shouldBeSuccess() },
        clock = clock,
        intervalFunction = { Duration.ofSeconds(it * 2L) }
    )

    private var invocations = 0
    private var task: (Int) -> Result4k<Int, String> = {
        invocations += 1
        Success(it)
    }

    @Test
    fun `succeed on first attempt`() {
        retries.attempt("id1", task) shouldBeSuccess 1
        invocations shouldBe 1
    }

    @Test
    fun `fail and then throttle`() {
        task = { Failure("foo") }

        retries.attempt("id1", task) shouldBeFailure RetryError.TaskError("foo")
    }
}