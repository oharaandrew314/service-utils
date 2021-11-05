package io.andrewohara.utils.queue

import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class QueueExecutor<Message>(
    private val queue: Queue<Message>,
    private val worker: Worker,
    private val task: Task<Message>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    operator fun invoke(): List<Future<Any>> {
        if (worker.availableConcurrency() <= 0) {
            log.trace("Worker isn't ready.  Sleeping")
            return emptyList()
        }

        val messages = try {
            queue.poll(worker.availableConcurrency())
        } catch (e: Exception) {
            log.error("Error polling for messages from $queue", e)
            throw e
        }

        return messages.map { item ->
            worker {
                try {
                    task(item)
                } catch (e: Exception) {
                    log.error("Error processing task", e)
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

        log.debug("Polling $queue every $pollInterval")

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