package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.*

class QueueExecutor<Message, Result, Item: QueueItem<Message>>(
    private val queue: WorkQueue<Message, Item>,
    private val bufferSize: Int = 10,
    private val onTaskError: (QueueItem<Message>, Throwable) -> Unit = { _, error -> error.printStackTrace() },
    private val onPollError: (Throwable) -> Unit = { it.printStackTrace() },
    private val onBatchComplete: (List<Result>) -> Unit = {},
    private val task: Task<Message, Result>,
    private val autoDeleteMessages: Boolean = true
) {
    fun executeNow(): Collection<Result> {
        val messages = try {
            queue(bufferSize)
        } catch (e: Throwable) {
            onPollError(e)
            emptyList()
        }

        val completed = messages.mapNotNull { item ->
            try {
                val result = task(item.message) { queue.setTimeout(item, it) }
                item to result
            } catch (e: Throwable) {
                onTaskError(item, e)
                null
            }
        }

        onBatchComplete(completed.map { it.second })
        if (autoDeleteMessages) queue -= completed.map { it.first }
        return completed.map { it.second }
    }

    fun start(workers: Int, interval: Duration? = null): ExecutorHandle {
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