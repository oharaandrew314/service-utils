package io.andrewohara.utils.queue

import io.andrewohara.utils.jdk.MutableFixedClock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class QueueExecutorTest {

    private val clock = MutableFixedClock(Instant.parse("2021-11-19T12:00:00Z"))
    private val queue = WorkQueue.memoryThreadSafe<String>(clock, lockFor = Duration.ofSeconds(10))

    private val task = Task<String> { work -> work.message }
    private val executor = QueueExecutor(queue) { task(it) }

    @Test
    fun `executeNow with empty queue`() {
        executor.executeNow().shouldBeEmpty()
    }

    @Test
    fun `executeNow with single item in queue`() {
        queue.send("do")

        executor.executeNow().shouldContainExactly("do")

        clock += Duration.ofSeconds(20)
        queue.poll(10).shouldBeEmpty()
    }

    @Test
    fun `executeNow with multiple items in queue`() {
        queue.send("do")
        queue.send("stuff")

        executor.executeNow().shouldContainExactly("do", "stuff")

        clock += Duration.ofSeconds(20)
        queue.poll(10).shouldBeEmpty()
    }

    @Test
    fun `executeNow with multiple items in queue and limited buffer size`() {
        queue.send("do")
        queue.send("stuff")

        val executor = QueueExecutor(queue, bufferSize = 1) { task(it) }
        executor.executeNow().shouldContainExactly("do")

        clock += Duration.ofSeconds(20)
        queue.poll(10).shouldHaveSize(1)
    }

    @Test
    fun `executeNow with multiple items in queue and autoDeleteMessage disabled`() {
        queue.send("do")
        queue.send("stuff")

        val executor = QueueExecutor(queue, autoDeleteMessage = false) { task(it) }
        executor.executeNow().shouldContainExactly("do", "stuff")

        clock += Duration.ofSeconds(20)
        queue.poll(10).shouldHaveSize(2)
    }
}