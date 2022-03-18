package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.ConcurrentLinkedQueue

fun <Message> WorkQueue.Companion.memorySingleReceive() = MemoryWorkQueue<Message>()

class MemoryWorkQueue<Message>: WorkQueue<Message> {

    private val queue = ConcurrentLinkedQueue<MemoryQueueItem<Message>>()

    override fun plusAssign(message: Message) {
        queue += MemoryQueueItem(message)
    }

    override fun invoke(maxMessages: Int): List<MemoryQueueItem<Message>> {
        if (maxMessages < 1) return emptyList()
        return (1..maxMessages).mapNotNull { queue.poll() }
    }

    override fun minusAssign(items: Collection<QueueItem<Message>>) {
        // no-op
    }

    override fun setTimeout(item: QueueItem<Message>, duration: Duration) {
        // no-op
    }
}

data class MemoryQueueItem<Message>(
    override val message: Message
): QueueItem<Message>