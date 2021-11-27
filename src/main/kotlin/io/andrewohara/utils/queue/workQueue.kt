package io.andrewohara.utils.queue

import java.time.Duration

fun interface Task<Message>: (QueueItem<Message>) -> Any
fun interface ExecutorHandle: (Duration?) -> Unit

interface WorkQueue<Message> {
    fun send(message: Message)
    fun poll(maxMessages: Int): List<QueueItem<Message>>

    companion object
}

interface QueueItem<Message> {
    val message: Message
    fun delete()
    fun extendLock(duration: Duration)
}

