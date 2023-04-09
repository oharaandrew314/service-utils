package io.andrewohara.utils.queue

import java.util.concurrent.ConcurrentLinkedQueue

fun <Message> WorkQueue.Companion.memorySingleReceive() = MemoryWorkQueue<Message>()

class MemoryWorkQueue<Message>: WorkQueue<Message> {

    private val queue = ConcurrentLinkedQueue<MemoryQueueItem<Message>>()

    override fun plusAssign(message: Message) {
        queue += MemoryQueueItem(message)
    }

    override fun plusAssign(messages: Collection<Message>) {
        queue += messages.map { MemoryQueueItem(it) }
    }

    override fun invoke(maxMessages: Int): List<MemoryQueueItem<Message>> {
        if (maxMessages <= 0) return emptyList()
        val results = mutableListOf<MemoryQueueItem<Message>>()

        do {
            val next = queue.poll()
            if (next != null) results += next
        } while (next != null && results.size < maxMessages)

        return results
    }

    override fun minusAssign(items: Collection<QueueItem<Message>>) {
        // no-op
    }
}

data class MemoryQueueItem<Message>(
    override val message: Message
): QueueItem<Message>