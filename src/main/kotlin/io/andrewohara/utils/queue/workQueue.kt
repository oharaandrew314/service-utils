package io.andrewohara.utils.queue

import java.time.Duration

typealias Task<Message> = (QueueItem<Message>) -> Any

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

interface ExecutorHandle {
    fun stop(timeout: Duration? = null)
}