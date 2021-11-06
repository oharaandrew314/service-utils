package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadPoolExecutor

typealias Task<Message> = (QueueItem<Message>) -> Any

interface Queue<Message> {
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

interface WorkerPool {
    fun available(): Int
    operator fun invoke(task: () -> Any): Future<Any>

    companion object {
        fun fixedThreads(threads: Int) = object: WorkerPool {
            val pool = Executors.newFixedThreadPool(threads) as ThreadPoolExecutor

            override fun available() = pool.corePoolSize - pool.activeCount
            override fun invoke(task: () -> Any) = pool.submit(task)
        }

        fun blocking(sequentialTasks: Int = 1) = object: WorkerPool {
            override fun available() = sequentialTasks
            override fun invoke(task: () -> Any) = CompletableFuture.completedFuture(task())
        }
    }
}