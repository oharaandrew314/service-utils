package io.andrewohara.utils.queue

import java.time.Duration

fun interface SetTimeout: (Duration) -> Unit
fun interface Task<Message, Result>: (Message, SetTimeout) -> Result
fun interface ExecutorHandle: (Duration?) -> Unit

interface WorkQueue<Message> {
    operator fun plusAssign(message: Message)
    operator fun invoke(maxMessages: Int): List<QueueItem<Message>>
    operator fun minusAssign(items: Collection<QueueItem<Message>>)
    operator fun minusAssign(item: QueueItem<Message>) = minusAssign(setOf(item))
    fun setTimeout(item: QueueItem<Message>, duration: Duration)

    companion object
}

interface QueueItem<Message> {
    val message: Message
}

