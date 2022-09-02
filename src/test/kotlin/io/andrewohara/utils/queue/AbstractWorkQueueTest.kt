package io.andrewohara.utils.queue

import io.andrewohara.utils.jdk.toClock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant

abstract class AbstractWorkQueueTest<Item: QueueItem<String>> {

    private val clock = Instant.parse("2021-11-19T12:00:00Z").toClock()
    private lateinit var queue: WorkQueue<String>

    abstract fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String>

    @BeforeEach
    fun setup() {
        queue = createQueue(clock, Duration.ofSeconds(10))
    }

    @Test
    fun `poll empty`() {
        queue.invoke(10).shouldBeEmpty()
    }

    @Test
    fun `poll after batch send`() {
        queue += listOf("foo", "bar")
        queue.invoke(10) shouldHaveSize 2
    }

    @Test
    fun `poll with multiple items`() {
        queue.plusAssign("foo")
        queue.plusAssign("bar")
        queue.invoke(10) shouldHaveSize 2
    }

    @Test
    fun `poll with more items than max receive count`() {
        queue.plusAssign("foo")
        queue.plusAssign("bar")
        queue.invoke(1) shouldHaveSize 1
    }

    @Test
    fun `poll should make message invisible`() {
        queue.plusAssign("foo")
        queue.invoke(1) shouldHaveSize 1
        queue.invoke(1).shouldBeEmpty()
    }

    @Test
    fun `invisible message should become visible after timeout`() {
        queue.plusAssign("foo")
        queue.invoke(1) shouldHaveSize 1

        clock += Duration.ofSeconds(20)
        queue.invoke(1) shouldHaveSize 1
    }

    @Test
    fun `message not visible after delete`() {
        queue.plusAssign("foo")
        val message = queue.invoke(1).shouldHaveSize(1).first()

        queue.minusAssign(message)
        clock += Duration.ofSeconds(20)

        queue.invoke(1).shouldBeEmpty()
    }

    @Test
    fun `extend message visibility`() {
        queue.plusAssign("foo")
        val message = queue.invoke(1).shouldHaveSize(1).first()

        clock += Duration.ofSeconds(5)
        queue.setTimeout(message, Duration.ofSeconds(10))
        clock += Duration.ofSeconds(7)

        queue.invoke(1).shouldBeEmpty()
    }
}