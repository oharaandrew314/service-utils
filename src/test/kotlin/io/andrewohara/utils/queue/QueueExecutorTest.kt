package io.andrewohara.utils.queue

import io.andrewohara.awsmock.sqs.MockSqsV2
import io.andrewohara.awsmock.sqs.backend.MockSqsBackend
import io.andrewohara.utils.jdk.MutableFixedClock
import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.jacksonJson
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class QueueExecutorTest {

    private val clock = MutableFixedClock(Instant.parse("2021-11-19T12:00:00Z"))

    private val sqsBackend = MockSqsBackend(clock)
    private val backendQueue = sqsBackend.create("test")!!

    private val queue = let {
        val sqs = MockSqsV2(sqsBackend)
        WorkQueue.sqsV2<String>(sqs, backendQueue.url, ValueMapper.jacksonJson())
    }

    private val task = Task<String, String> { work, _ -> work }
    private val executor = queue.poll(task = task)

    @Test
    fun `executeNow with empty queue`() {
        executor.executeNow().shouldBeEmpty()
    }

    @Test
    fun `executeNow with single item in queue`() {
        queue.plusAssign("do")

        executor.executeNow().shouldContainExactly("do")

        clock += Duration.ofSeconds(20)
        queue.invoke(10).shouldBeEmpty()
    }

    @Test
    fun `executeNow with multiple items in queue`() {
        queue.plusAssign("do")
        queue.plusAssign("stuff")

        executor.executeNow().shouldContainExactly("do", "stuff")

        clock += Duration.ofSeconds(20)
        queue.invoke(10).shouldBeEmpty()
    }

    @Test
    fun `executeNow with multiple items in queue and limited buffer size`() {
        queue.plusAssign("do")
        queue.plusAssign("stuff")

        val executor = queue.poll(bufferSize = 1, task = task)
        executor.executeNow().shouldContainExactly("do")

        clock += Duration.ofSeconds(20)
        queue.invoke(10).shouldHaveSize(1)
    }
}