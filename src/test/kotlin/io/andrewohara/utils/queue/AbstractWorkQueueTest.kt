package io.andrewohara.utils.queue

import io.andrewohara.utils.jdk.MutableFixedClock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant

abstract class AbstractWorkQueueTest {

    private val clock = MutableFixedClock(Instant.parse("2021-11-19T12:00:00Z"))
    private lateinit var queue: WorkQueue<String>

    abstract fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String>

    @BeforeEach
    fun setup() {
        queue = createQueue(clock, Duration.ofSeconds(10))
    }

    @Test
    fun `poll empty`() {
        queue.poll(10).shouldBeEmpty()
    }

    @Test
    fun `poll with multiple items`() {
        queue.send("foo")
        queue.send("bar")
        queue.poll(10) shouldHaveSize 2
    }

    @Test
    fun `poll with more items than max receive count`() {
        queue.send("foo")
        queue.send("bar")
        queue.poll(1) shouldHaveSize 1
    }

    @Test
    fun `poll should make message invisible`() {
        queue.send("foo")
        queue.poll(1) shouldHaveSize 1
        queue.poll(1).shouldBeEmpty()
    }

    @Test
    fun `invisible message should become visible after timeout`() {
        queue.send("foo")
        queue.poll(1) shouldHaveSize 1

        clock += Duration.ofSeconds(20)
        queue.poll(1) shouldHaveSize 1
    }

    @Test
    fun `message not visible after delete`() {
        queue.send("foo")
        val message = queue.poll(1).shouldHaveSize(1).first()

        message.delete()
        clock += Duration.ofSeconds(20)

        queue.poll(1).shouldBeEmpty()
    }

    @Test
    fun `extend message visibility`() {
        queue.send("foo")
        val message = queue.poll(1).shouldHaveSize(1).first()

        clock += Duration.ofSeconds(5)
        message.extendLock(Duration.ofSeconds(10))
        clock += Duration.ofSeconds(7)

        queue.poll(1).shouldBeEmpty()
    }
}