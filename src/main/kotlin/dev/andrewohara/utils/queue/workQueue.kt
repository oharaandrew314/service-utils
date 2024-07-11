package dev.andrewohara.utils.queue

fun interface BatchTask<Message>: (List<QueueItem<Message>>) -> Collection<TaskResult<Message>>
sealed interface TaskResult<Message> {
    val item: QueueItem<Message>

    data class Success<Message>(override val item: QueueItem<Message>): TaskResult<Message>
    data class Failure<Message>(override val item: QueueItem<Message>, val message: String, val throwable: Throwable? = null): TaskResult<Message>
}

interface WorkQueue<Message> {
    operator fun plusAssign(message: Message)
    operator fun plusAssign(messages: Collection<Message>)
    operator fun invoke(maxMessages: Int): List<QueueItem<Message>>
    operator fun minusAssign(items: Collection<QueueItem<Message>>)
    operator fun minusAssign(item: QueueItem<Message>) = minusAssign(setOf(item))

    companion object
}

interface QueueItem<Message> {
    val message: Message
}

typealias TaskErrorHandler<Message> = (TaskResult.Failure<Message>) -> Unit

typealias ErrorHandler = (Throwable) -> Unit

