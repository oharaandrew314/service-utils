package io.andrewohara.utils.queue

import io.andrewohara.awsmock.sqs.MockSqsV2
import io.andrewohara.awsmock.sqs.backend.MockSqsBackend
import io.andrewohara.utils.jdk.MutableFixedClock
import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.jacksonJson
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class QueueExecutorTest {

    private val clock = MutableFixedClock(Instant.parse("2021-11-19T12:00:00Z"))

    private val sqsBackend = MockSqsBackend(clock)
    private val backendQueue = sqsBackend.create("test")!!

    private val queue = let {
        val sqs = MockSqsV2(sqsBackend)
        WorkQueue.sqsV2<String>(sqs, backendQueue.url, ValueMapper.jacksonJson())
    }

    private val taskErrors = mutableListOf<TaskResult.Failure<String>>()
    private val completedTasks = mutableListOf<String>()
    private val executor = queue.withWorker(
        errorHandler = { throw it },
        taskErrorHandler = { taskErrors += it },
        task = { completedTasks += it }
    )

    @Test
    fun `invoke with empty queue`() {
        executor()
        completedTasks.shouldBeEmpty()
    }

    @Test
    fun `invoke with single item in queue`() {
        queue.plusAssign("do")

        executor()
        completedTasks.shouldContainExactly("do")

        clock += Duration.ofSeconds(20)
        queue.invoke(10).shouldBeEmpty()
    }

    @Test
    fun `invoke with multiple items in queue`() {
        queue.plusAssign("do")
        queue.plusAssign("stuff")

        executor()
        completedTasks.shouldContainExactly("do", "stuff")

        clock += Duration.ofSeconds(20)
        queue.invoke(10).shouldBeEmpty()
    }

    @Test
    fun `invoke with multiple items in queue and limited buffer size`() {
        queue.plusAssign("do")
        queue.plusAssign("stuff")

        queue.withWorker(
            errorHandler = { throw it},
            taskErrorHandler = {error(it)},
            bufferSize = 1
        ) { work: String ->
            completedTasks += work
        }.invoke()
        completedTasks.shouldContainExactly("do")

        clock += Duration.ofSeconds(20)
        queue.invoke(10).shouldHaveSize(1)
    }

    @Test
    fun `invoke with failures and successes`() {
        queue += listOf("foo", "bar")

        queue.withWorker(
            errorHandler = { throw it },
            taskErrorHandler = { taskErrors += it },
            task = { work: String ->
                when(work) {
                    "foo" -> completedTasks += work
                    else -> throw IllegalArgumentException(work)
                }
            }
        ).invoke()

        completedTasks.shouldContainExactly("foo")
        taskErrors
            .shouldHaveSize(1)
            .map { failure ->
                failure.throwable shouldBe IllegalArgumentException("bar")
                failure.message shouldBe "Unexpected failure"
            }
    }

    @Test
    fun `process queue async`() {
        val latch = CountDownLatch(2)

        queue += listOf("foo", "bar")

        queue.withWorker(
            errorHandler = { throw it },
            taskErrorHandler = { error(it) }
        ) { latch.countDown() }.start(1)

        latch.await(5, TimeUnit.SECONDS) shouldBe true
    }

    @Test
    fun `process queue batch async`() {
        val latch = CountDownLatch(2)

        queue += listOf("foo", "bar")

        queue.withBatchWorker(
            errorHandler = { throw it },
            taskErrorHandler = { error(it) }
        ) { batch ->
            batch.map { task ->
                latch.countDown()
                TaskResult.Success(task)
            }
        }.start(1)

        latch.await(5, TimeUnit.SECONDS) shouldBe true
    }

    @Test
    fun `process queue with worker that returns result`() {
        queue += listOf("foo", "bar")

        queue.withWorkerToResult(
            errorHandler = { throw it },
            taskErrorHandler = { taskErrors += it },
            task = { work: QueueItem<String> ->
                when(work.message) {
                    "foo" -> {
                        completedTasks += work.message
                        TaskResult.Success(work)
                    }
                    else -> TaskResult.Failure(work, "bad")
                }
            }
        ).invoke()

        completedTasks.shouldContainExactly("foo")
        taskErrors
            .shouldHaveSize(1)
            .map { failure ->
                failure.throwable shouldBe null
                failure.message shouldBe "bad"
            }
    }
}