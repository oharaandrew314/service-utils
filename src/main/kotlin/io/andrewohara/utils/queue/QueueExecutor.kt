package io.andrewohara.utils.queue

import java.time.Duration
import java.util.concurrent.*

fun <Message> WorkQueue<Message>.withWorker(
    errorHandler: ErrorHandler = { it.printStackTrace() },
    taskErrorHandler: TaskErrorHandler<Message> = { println(it) },
    bufferSize: Int = 10,
    task: (Message) -> Unit
) = QueueExecutor(
    queue = this,
    taskErrorHandler = taskErrorHandler,
    errorHandler = errorHandler,
    bufferSize = bufferSize,
    batchTask = { batch ->
        batch.map { item ->
            try {
                task(item.message)
                TaskResult.Success(item)
            } catch (e: Throwable) {
                TaskResult.Failure(item, "Unexpected failure", e)
            }
        }
    }
)

fun <Message> WorkQueue<Message>.withBatchWorker(
    errorHandler: ErrorHandler = { it.printStackTrace() },
    taskErrorHandler: TaskErrorHandler<Message> = { println(it) },
    bufferSize: Int = 10,
    batchTask: BatchTask<Message>
) = QueueExecutor(
    queue = this,
    taskErrorHandler = taskErrorHandler,
    errorHandler = errorHandler,
    bufferSize = bufferSize,
    batchTask = batchTask
)

class QueueExecutor<Message>(
    private val queue: WorkQueue<Message>,
    private val bufferSize: Int = 10,
    private val errorHandler: ErrorHandler,
    private val taskErrorHandler: TaskErrorHandler<Message>,
    private val batchTask: BatchTask<Message>
) {
    operator fun invoke() = try {
        val messages = queue(bufferSize)
        val results = batchTask(messages)
        queue -= results.mapNotNull { result ->
            when(result) {
                is TaskResult.Failure<Message> -> {
                    taskErrorHandler(result)
                    null
                }
                is TaskResult.Success<Message> -> result.item
            }
        }
    } catch (e: Throwable) {
        errorHandler(e)
    }

    fun start(workers: Int, interval: Duration? = null): ExecutorHandle {
        if (workers < 1) return ExecutorHandle {  }

        val executor = Executors.newCachedThreadPool()
        repeat(workers) {
            executor.submit {
                while (!Thread.currentThread().isInterrupted) {
                    invoke()
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