package dev.andrewohara.utils.queue

import org.http4k.format.AutoMarshalling
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry
import java.io.IOException
import java.time.Duration
import kotlin.reflect.KClass

private const val MAX_RECEIVE_COUNT = 10  // SQS has a limit to the number of messages to receive
private const val MAX_BATCH_SIZE = 10

inline fun <reified Message: Any> WorkQueue.Companion.sqsV2(
    sqs: SqsClient,
    url: String,
    marshaller: AutoMarshalling,
    pollWaitTime: Duration? = Duration.ofSeconds(10),
    deliveryDelay: Duration? =  null,
    noinline getGroupId: (Message) -> String? = { null },
    noinline getDeduplicationId: (Message) -> String? = { null },
) = SqsV2WorkQueue(
    sqs = sqs,
    url = url,
    marshaller = marshaller,
    pollWaitTime = pollWaitTime,
    deliveryDelay = deliveryDelay,
    type = Message::class,
    getGroupId = getGroupId,
    getDeduplicationId = getDeduplicationId
)

class SqsV2WorkQueue<Message: Any>(
    private val sqs: SqsClient,
    private val url: String,
    private val marshaller: AutoMarshalling,
    private val pollWaitTime: Duration?,
    private val deliveryDelay: Duration?,
    private val type: KClass<Message>,
    private val getGroupId: (Message) -> String? = { null },
    private val getDeduplicationId: (Message) -> String? = { null }
): WorkQueue<Message> {

    override fun invoke(maxMessages: Int): List<SqsV2QueueItem<Message>> {
        val response = sqs.receiveMessage {
            it.queueUrl(url)
            it.maxNumberOfMessages(maxMessages.coerceAtMost(MAX_RECEIVE_COUNT))
            it.waitTimeSeconds(pollWaitTime?.toSeconds()?.toInt())
        }

        return response.messages().mapNotNull { message ->
            SqsV2QueueItem(
                messageId = message.messageId(),
                message = marshaller.asA(message.body(), type),
                receiptHandle = message.receiptHandle()
            )
        }
    }

    override fun minusAssign(items: Collection<QueueItem<Message>>) {
        val entries = items.filterIsInstance<SqsV2QueueItem<Message>>().map { item ->
            DeleteMessageBatchRequestEntry.builder()
                .id(item.messageId)
                .receiptHandle(item.receiptHandle)
                .build()
        }

        if (entries.isEmpty()) return

        sqs.deleteMessageBatch {
            it.queueUrl(url)
            it.entries(entries)
        }
    }

    override fun plusAssign(message: Message) {
        sqs.sendMessage {
            it.queueUrl(url)
            it.messageBody(marshaller.asFormatString(message))
            it.delaySeconds(deliveryDelay?.toSeconds()?.toInt())
            it.messageGroupId(getGroupId(message))
            it.messageDeduplicationId(getDeduplicationId(message))
        }
    }

    override fun plusAssign(messages: Collection<Message>) {
        for (batch in messages.chunked(MAX_BATCH_SIZE)) {
            val entries = batch
                .withIndex()
                .map { (index, message) ->
                    SendMessageBatchRequestEntry.builder()
                        .id(index.toString())
                        .delaySeconds(deliveryDelay?.toSeconds()?.toInt())
                        .messageBody(marshaller.asFormatString(message))
                        .messageGroupId(getGroupId(message))
                        .messageDeduplicationId(getDeduplicationId(message))
                        .build()
                }

            val result = sqs.sendMessageBatch {
                it.queueUrl(url)
                it.entries(entries)
            }
            if (result.hasFailed()) {
                throw IOException("Error sending messages: $result")
            }
        }
    }

    override fun toString() = "${javaClass.simpleName}: $url"
}

data class SqsV2QueueItem<Message>(
    val messageId: String,
    override val message: Message,
    val receiptHandle: String
): QueueItem<Message>