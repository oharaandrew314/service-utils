package io.andrewohara.utils.queue

import io.andrewohara.awsmock.sqs.MockSqsV1
import io.andrewohara.awsmock.sqs.backend.MockSqsBackend
import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.moshi
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

class QueueExecutorTest {

    data class Work(
        val id: Int,
        val action: String
    )

    private val sqs = MockSqsBackend()
    private val sqsQueue = sqs.create("work")!!

    private val queue = SqsV1Queue<Work>(
        sqs = MockSqsV1(sqs),
        queueUrl = sqsQueue.url,
        mapper = ValueMapper.moshi()
    )

    private val task: Task<Work> = { work -> work.message.action }

    private val blockingExecutor = QueueExecutor(queue, WorkerPool.blocking(), task)
    private val threadedExecutor = QueueExecutor(queue, WorkerPool.fixedThreads(2), task)

    @Test
    fun `empty queue for blocking executor`() {
        blockingExecutor().shouldBeEmpty()
    }

    @Test
    fun `single item in queue for blocking executor`() {
        queue.send(Work(1, "do"))

        blockingExecutor()
            .shouldHaveSize(1)
            .map { it.get() }
            .shouldContainExactly("do")
    }

    @Test
    fun `multiple items in queue for blocking executor`() {
        queue.send(Work(1, "do"))
        queue.send(Work(2, "stuff"))

        blockingExecutor()
            .shouldHaveSize(1)
            .map { it.get() }
            .shouldContainExactly("do")
    }

    @Test
    fun `multiple items in queue for threaded executor`() {
        queue.send(Work(1, "do"))
        queue.send(Work(2, "stuff"))

        threadedExecutor()
            .shouldHaveSize(2)
            .map { it.get() }
            .shouldContainExactly("do", "stuff")
    }
}