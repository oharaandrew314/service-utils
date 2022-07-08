package io.andrewohara.utils.queue

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

class MemoryWorkQueueTest {

    private val queue = WorkQueue.memorySingleReceive<String>()

    @Test
    fun `poll 0 messages`() {
        queue.plusAssign("foo")
        queue.plusAssign("bar")
        queue.invoke(0).shouldBeEmpty()
    }

    @Test
    fun `poll empty queue`() {
        queue.invoke(10).shouldBeEmpty()
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
}