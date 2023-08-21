package io.andrewohara.utils.queue

import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.jacksonJson
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.http4k.aws.AwsSdkClient
import org.http4k.connect.amazon.sqs.FakeSQS
import org.junit.jupiter.api.Test
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.services.sqs.SqsClient
import java.lang.IllegalArgumentException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class QueueExecutorTest {

    private val queue = let {
        val sqs = SqsClient.builder()
            .httpClient(AwsSdkClient(FakeSQS()))
            .credentialsProvider { AwsBasicCredentials.create("id", "secret") }
            .build()

        val url = sqs.createQueue {
            it.queueName("test")
        }.queueUrl()

        WorkQueue.sqsV2<String>(sqs, url, ValueMapper.jacksonJson())
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

        queue.invoke(10).shouldBeEmpty()
    }

    @Test
    fun `invoke with multiple items in queue`() {
        queue.plusAssign("do")
        queue.plusAssign("stuff")

        executor()
        completedTasks.shouldContainExactly("do", "stuff")

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