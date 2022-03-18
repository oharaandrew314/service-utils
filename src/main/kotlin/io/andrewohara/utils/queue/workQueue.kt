package io.andrewohara.utils.queue

import java.time.Duration

fun interface SetTimeout: (Duration) -> Unit
fun interface Task<Message, Result>: (Message, SetTimeout) -> Result
fun interface ExecutorHandle: (Duration?) -> Unit

interface WorkQueue<Message, Item: QueueItem<Message>> {
    operator fun plusAssign(message: Message)
    operator fun invoke(maxMessages: Int): List<Item>
    operator fun minusAssign(items: Collection<Item>)
    operator fun minusAssign(item: Item) = minusAssign(setOf(item))
    fun setTimeout(item: Item, duration: Duration)

    companion object
}

interface QueueItem<Message> {
    val message: Message
}

