package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue

fun <Message> WorkQueue.Companion.memorySingleReceive() = MemoryWorkQueue<Message>()

class MemoryWorkQueue<Message>: WorkQueue<Message, MemoryQueueItem<Message>> {

    private val queue = ConcurrentLinkedQueue<MemoryQueueItem<Message>>()

    override fun plusAssign(message: Message) {
        queue += MemoryQueueItem(message)
    }

    override fun invoke(maxMessages: Int): List<MemoryQueueItem<Message>> {
        if (maxMessages < 1) return emptyList()
        return (1..maxMessages).mapNotNull { queue.poll() }
    }

    override fun minusAssign(items: Collection<MemoryQueueItem<Message>>) {
        // no-op
    }

    override fun setTimeout(item: MemoryQueueItem<Message>, duration: Duration) {
        // no-op
    }
}

data class MemoryQueueItem<Message>(
    override val message: Message
): QueueItem<Message>