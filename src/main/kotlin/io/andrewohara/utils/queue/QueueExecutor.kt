package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class QueueExecutor<Message>(
    private val queue: Queue<Message>,
    private val workerPool: WorkerPool,
    private val task: Task<Message>,
    private val onTaskError: (Task<Message>, Exception) -> Unit = { _, _ -> },
    private val onPollError: (Exception) -> Unit = {}
) {
    operator fun invoke(): List<Future<Any>> {
        if (workerPool.available() <= 0) {
            return emptyList()
        }

        val messages = try {
            queue.poll(workerPool.available())
        } catch (e: Exception) {
            onPollError(e)
            emptyList()
        }

        return messages.map { item ->
            workerPool {
                try {
                    task(item)
                } catch (e: Exception) {
                    onTaskError(task, e)
                }
            }
        }
    }

    fun scheduleEvery(pollInterval: Duration): ExecutorHandle {
        val scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduler.scheduleWithFixedDelay(
            ::invoke,
            pollInterval.toMillis(),
            pollInterval.toMillis(),
            TimeUnit.MILLISECONDS
        )

        return object: ExecutorHandle {
            override fun stop(timeout: Duration?) {
                scheduler.shutdown()
                if (timeout != null) {
                    scheduler.awaitTermination(timeout.seconds, TimeUnit.SECONDS)
                }
            }

        }
    }
}