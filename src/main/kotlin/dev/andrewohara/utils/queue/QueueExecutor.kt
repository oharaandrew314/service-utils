package dev.andrewohara.utils.queue

import dev.andrewohara.utils.IdGenerator
import java.time.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

fun <Message> WorkQueue<Message>.withWorker(
    errorHandler: ErrorHandler,
    taskErrorHandler: TaskErrorHandler<Message>,
    bufferSize: Int = 10,
    threadFactory: ThreadFactory = Executors.defaultThreadFactory(),
    task: (Message) -> Unit
) = QueueExecutor(
    queue = this,
    taskErrorHandler = taskErrorHandler,
    errorHandler = errorHandler,
    bufferSize = bufferSize,
    threadFactory = threadFactory,
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

fun <Message> WorkQueue<Message>.withWorkerToResult(
    errorHandler: ErrorHandler,
    taskErrorHandler: TaskErrorHandler<Message>,
    bufferSize: Int = 10,
    threadFactory: ThreadFactory = Executors.defaultThreadFactory(),
    task: (QueueItem<Message>) -> TaskResult<Message>
) = QueueExecutor(
    queue = this,
    taskErrorHandler = taskErrorHandler,
    errorHandler = errorHandler,
    bufferSize = bufferSize,
    threadFactory = threadFactory,
    batchTask = { batch ->
        batch.map { item ->
            try {
                task(item)
            } catch (e: Throwable) {
                TaskResult.Failure(item, "Unexpected failure", e)
            }
        }
    }
)

fun <Message> WorkQueue<Message>.withBatchWorker(
    errorHandler: ErrorHandler,
    taskErrorHandler: TaskErrorHandler<Message>,
    bufferSize: Int = 10,
    threadFactory: ThreadFactory = Executors.defaultThreadFactory(),
    batchTask: BatchTask<Message>
) = QueueExecutor(
    queue = this,
    taskErrorHandler = taskErrorHandler,
    errorHandler = errorHandler,
    bufferSize = bufferSize,
    batchTask = batchTask,
    threadFactory = threadFactory
)

class QueueExecutor<Message>(
    private val queue: WorkQueue<Message>,
    private val bufferSize: Int = 10,
    private val errorHandler: ErrorHandler,
    private val taskErrorHandler: TaskErrorHandler<Message>,
    private val batchTask: BatchTask<Message>,
    private val name: String = "Executor ${IdGenerator.nextBase36(4)}",
    private val threadFactory: ThreadFactory
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
        Thread.sleep(1_000)
    }

    fun start(workers: Int, interval: Duration? = null) {
        require(workers > 0) { "Cannot start Executor without any workers" }

        repeat(workers) {num ->
            val thread = threadFactory.newThread {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        invoke()
                    } catch (e: Throwable) {
                        errorHandler(e)
                    }
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
            thread.name = "${javaClass.simpleName}:$name-$num"
            thread.start()
        }
    }

    override fun toString() = name
}