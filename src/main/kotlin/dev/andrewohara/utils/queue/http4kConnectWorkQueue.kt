package dev.andrewohara.utils.queue

import dev.forkhandles.result4k.onFailure
import org.http4k.connect.amazon.sqs.SQS
import org.http4k.connect.amazon.sqs.action.SendMessageBatchEntry
import org.http4k.connect.amazon.sqs.deleteMessageBatch
import org.http4k.connect.amazon.sqs.model.ReceiptHandle
import org.http4k.connect.amazon.sqs.model.SQSMessageId
import org.http4k.connect.amazon.sqs.receiveMessage
import org.http4k.connect.amazon.sqs.sendMessage
import org.http4k.connect.amazon.sqs.sendMessageBatch
import org.http4k.core.Uri
import org.http4k.format.AutoMarshalling
import java.time.Duration
import kotlin.reflect.KClass

private const val MAX_MESSAGES = 10  // SQS-imposed limit to send and receive

inline fun <reified Message: Any> WorkQueue.Companion.http4k(
    sqs: SQS,
    url: Uri,
    marshaller: AutoMarshalling,
    pollWaitTime: Duration? = Duration.ofSeconds(20),
    deliveryDelay: Duration? =  null,
    noinline getGroupId: (Message) -> String? = { null },
    noinline getDeduplicationId: (Message) -> String? = { null },
) = Http4kConnectWorkQueue(
    sqs = sqs,
    url = url,
    marshaller = marshaller,
    pollWaitTime = pollWaitTime,
    deliveryDelay = deliveryDelay,
    type = Message::class,
    getGroupId = getGroupId,
    getDeduplicationId = getDeduplicationId
)

class Http4kConnectWorkQueue<Message: Any>(
    private val sqs: SQS,
    private val url: Uri,
    private val marshaller: AutoMarshalling,
    private val pollWaitTime: Duration?,
    private val deliveryDelay: Duration?,
    private val type: KClass<Message>,
    private val getGroupId: (Message) -> String? = { null },
    private val getDeduplicationId: (Message) -> String? = { null }
): WorkQueue<Message> {

    override fun invoke(maxMessages: Int): List<Http4kConnectWorkQueueItem<Message>> {
        val messages = sqs.receiveMessage(
            queueUrl = url,
            maxNumberOfMessages = maxMessages.coerceAtMost(MAX_MESSAGES) ,
            waitTimeSeconds = pollWaitTime?.toSeconds()?.toInt()
        ).onFailure { it.reason.throwIt() }


        return messages.map { message ->
            Http4kConnectWorkQueueItem(
                messageId = message.messageId,
                message = marshaller.asA(message.body, type),
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
            payload = marshaller.asFormatString(message),
            delaySeconds = deliveryDelay?.toSeconds()?.toInt(),
            messageGroupId = getGroupId(message),
            deduplicationId = getDeduplicationId(message)
        ).onFailure { it.reason.throwIt() }
    }

    override fun plusAssign(messages: Collection<Message>) {
        for (chunk in messages.chunked(MAX_MESSAGES)) {
            sqs.sendMessageBatch(
                queueUrl = url,
                entries = chunk.mapIndexed { index, message ->
                    SendMessageBatchEntry(
                        id = index.toString(),
                        payload = marshaller.asFormatString(message),
                        delaySeconds = deliveryDelay?.toSeconds()?.toInt(),
                        messageGroupId = getGroupId(message),
                        deduplicationId = getDeduplicationId(message)
                    )
                }
            ).onFailure { it.reason.throwIt() }
        }
    }

    override fun toString() = "${javaClass.simpleName}: $url"
}

data class Http4kConnectWorkQueueItem<Message>(
    val messageId: SQSMessageId,
    override val message: Message,
    val receiptHandle: ReceiptHandle
): QueueItem<Message>