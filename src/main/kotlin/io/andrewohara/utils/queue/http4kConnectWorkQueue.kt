package io.andrewohara.utils.queue

import dev.forkhandles.result4k.onFailure
import io.andrewohara.utils.mappers.ValueMapper
import org.http4k.connect.amazon.sqs.SQS
import org.http4k.connect.amazon.sqs.deleteMessageBatch
import org.http4k.connect.amazon.sqs.model.ReceiptHandle
import org.http4k.connect.amazon.sqs.model.SQSMessageId
import org.http4k.connect.amazon.sqs.receiveMessage
import org.http4k.connect.amazon.sqs.sendMessage
import org.http4k.core.Uri
import java.time.Duration

fun <Message> WorkQueue.Companion.http4k(
    sqs: SQS,
    url: Uri,
    mapper: ValueMapper<Message>,
    pollWaitTime: Duration = Duration.ofSeconds(20),
    deliveryDelay: Duration? =  null,
) = Http4kConnectWorkQueue(
    sqs = sqs,
    url = url,
    mapper = mapper,
    pollWaitTime = pollWaitTime,
    deliveryDelay = deliveryDelay
)

class Http4kConnectWorkQueue<Message>(
    private val sqs: SQS,
    private val url: Uri,
    private val mapper: ValueMapper<Message>,
    private val pollWaitTime: Duration,
    private val deliveryDelay: Duration?,
): WorkQueue<Message> {

    companion object {
        private const val maxReceiveCount = 10  // SQS has a limit to the number of messages to receive
//        private const val maxBatchSize = 10
    }

    override fun invoke(maxMessages: Int): List<Http4kConnectWorkQueueItem<Message>> {
        val messages = sqs.receiveMessage(
            queueUrl = url,
            maxNumberOfMessages = maxMessages.coerceAtMost(maxReceiveCount) ,
            longPollTime = pollWaitTime
        ).onFailure { it.reason.throwIt() }


        return messages.mapNotNull { message ->
            Http4kConnectWorkQueueItem(
                messageId = message.messageId,
                message = mapper.read(message.body) ?: return@mapNotNull null,
                receiptHandle = message.receiptHandle
            )
        }
    }

    override fun minusAssign(items: Collection<QueueItem<Message>>) {
        val entries = items
            .filterIsInstance<Http4kConnectWorkQueueItem<Message>>()
            .map { it.messageId to it.receiptHandle }

        if (entries.isEmpty()) return

        sqs.deleteMessageBatch(url,entries)
    }

    override fun plusAssign(message: Message) {
        sqs.sendMessage(
            queueUrl = url,
            payload = mapper.write(message),
            delaySeconds = deliveryDelay?.toSeconds()?.toInt()
        )
    }

    override fun plusAssign(messages: Collection<Message>) {
        messages.forEach(::plusAssign) // TODO batch
    }

    override fun toString() = "${javaClass.simpleName}: $url"
}

data class Http4kConnectWorkQueueItem<Message>(
    val messageId: SQSMessageId,
    override val message: Message,
    val receiptHandle: ReceiptHandle
): QueueItem<Message>