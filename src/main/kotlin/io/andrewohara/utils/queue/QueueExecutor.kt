package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.*

class QueueExecutor<Message>(
    private val queue: WorkQueue<Message>,
    private val bufferSize: Int = 10,
    private val onTaskError: (QueueItem<Message>, Throwable) -> Unit = { _, error -> error.printStackTrace() },
    private val onPollError: (Throwable) -> Unit = { it.printStackTrace() },
    private val autoDeleteMessage: Boolean = true,
    private val interval: Duration? = null,
    private val task: Task<Message>
) {
    fun executeNow(): List<Any?> {
        val messages = try {
            queue.poll(bufferSize)
        } catch (e: Throwable) {
            onPollError(e)
            emptyList()
        }

        return messages.map { message ->
            try {
                val result = task(message)
                if (autoDeleteMessage) message.delete()
                result
            } catch (e: Throwable) {
                onTaskError(message, e)
                null
            }
        }
    }

    fun start(workers: Int): ExecutorHandle {
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