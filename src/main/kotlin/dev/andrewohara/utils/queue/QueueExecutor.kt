package dev.andrewohara.utils.queue

import dev.andrewohara.utils.IdGenerator
import java.io.Closeable
import java.time.Duration
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
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

    private val threads = mutableListOf<Thread>()
    val isRunning: Boolean get() = threads.isNotEmpty()

    fun start(workers: Int, interval: Duration? = null): Closeable {
        require(workers > 0) { "No workers" }
        require(!isRunning) { "Already running" }

        synchronized(threads) {
            repeat(workers) { num ->
                val thread = threadFactory.newThread {
                    while (!Thread.currentThread().isInterrupted) {
                        try {
                            invoke()

                            if (interval != null) {
                                Thread.sleep(interval.toMillis())
                            }
                        } catch (e: InterruptedException) {
                            // allow this error to throw
                            Thread.currentThread().interrupt()
                            throw e
                        }
                        catch (e: Throwable) {
                            errorHandler(e)
                        }
                    }
                }
                thread.name = "${javaClass.simpleName}:$name-$num"
                thread.start()
                threads += thread
            }
        }

        return Closeable { stop() }
    }

    fun stop(timeout: Duration? = null) {
        synchronized(threads) {
            for (thread in threads) {
                thread.interrupt()
                if (timeout != null) {
                    thread.join(timeout)
                } else {
                    thread.join()
                }
            }
            threads.clear()
        }
    }

    override fun toString() = name
}