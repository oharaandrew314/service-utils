package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor

typealias Task<Message> = (QueueMessage<Message>) -> Any

interface Queue<Message> {
    fun poll(maxMessages: Int): List<QueueMessage<Message>>
    fun send(message: Message)

    companion object
}

interface QueueMessage<Message> {
    val body: Message
    fun delete()
    fun delayRetry(duration: Duration)
}

interface ExecutorHandle {
    fun stop(timeout: Duration? = null)
}

interface QueueMessageMapper<Message> {
    fun toQueueBody(message: Message): String
    fun toMessage(body: String): Message

    companion object {
        fun string() = object: QueueMessageMapper<String> {
            override fun toQueueBody(message: String) = message
            override fun toMessage(body: String) = body
        }
    }
}

interface Worker {
    fun availableConcurrency(): Int
    operator fun invoke(task: () -> Any): Future<Any>

    companion object {
        fun fixedThreads(threads: Int) = object: Worker {
            val pool = Executors.newFixedThreadPool(threads) as ThreadPoolExecutor

            override fun availableConcurrency() = pool.corePoolSize - pool.activeCount
            override fun invoke(task: () -> Any) = pool.submit(task)
        }

        fun blocking(sequentialTasks: Int = 1) = object: Worker {
            override fun availableConcurrency() = sequentialTasks
            override fun invoke(task: () -> Any) = CompletableFuture.completedFuture(task())
        }
    }
}