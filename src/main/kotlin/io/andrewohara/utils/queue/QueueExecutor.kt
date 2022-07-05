package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.*

fun <Message, Result> WorkQueue<Message>.withWorker(
    errorHandler: (Throwable) -> Unit = { it.printStackTrace() },
    taskErrorHandler: (Message, Throwable) -> Unit = { _, e -> errorHandler(e) },
    bufferSize: Int = 10,
    task: Task<Message, Result>
) = QueueExecutor(
    queue = this,
    taskErrorHandler = taskErrorHandler,
    errorHandler = errorHandler,
    bufferSize = bufferSize,
    task = task
)

class QueueExecutor<Message, Result>(
    private val queue: WorkQueue<Message>,
    private val bufferSize: Int = 10,
    private val errorHandler: (Throwable) -> Unit = { it.printStackTrace() },
    private val taskErrorHandler: (Message, Throwable) -> Unit = { _, e -> errorHandler(e) },
    private val task: Task<Message, Result>
) {
    fun executeNow(): Collection<Result> {
        val messages = try {
            queue(bufferSize)
        } catch (e: Throwable) {
            errorHandler(e)
            emptyList()
        }

        val completed = messages.mapNotNull { item ->
            try {
                val result = task(item.message) { queue.setTimeout(item, it) }
                item to result
            } catch (e: Throwable) {
                taskErrorHandler(item.message, e)
                null
            }
        }

        try {
            queue -= completed.map { it.first }
        } catch (e: Throwable) {
            errorHandler(e)
        }

        return completed.map { it.second }
    }

    fun start(workers: Int, interval: Duration? = null): ExecutorHandle {
        if (workers < 1) return ExecutorHandle {  }

        val executor = Executors.newCachedThreadPool()
        repeat(workers) {
            executor.submit {
                while (!Thread.currentThread().isInterrupted) {
                    executeNow()
                    if (interval != null) {
                        try {
                            Thread.sleep(interval.toMillis())
                        } catch (e: InterruptedException) {
                            Thread.currentThread().interrupt()
                            break
                        }
                    }
                }
            }
        }

        return ExecutorHandle { timeout ->
            executor.shutdown()
            if (timeout != null) {
                executor.awaitTermination(timeout.seconds, TimeUnit.SECONDS)
            }
        }
    }
}