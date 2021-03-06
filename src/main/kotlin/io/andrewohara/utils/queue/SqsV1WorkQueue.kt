package io.andrewohara.utils.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import io.andrewohara.utils.mappers.ValueMapper
import java.time.Duration

fun <Message> WorkQueue.Companion.sqsV1(
    sqs: AmazonSQS,
    url: String,
    mapper: ValueMapper<Message>,
    pollWaitTime: Duration = Duration.ofSeconds(20),
    deliveryDelay: Duration? =  null,
) = SqsV1WorkQueue(
    sqs = sqs,
    url = url,
    mapper = mapper,
    pollWaitTime = pollWaitTime,
    deliveryDelay = deliveryDelay
)

class SqsV1WorkQueue<Message>(
    private val sqs: AmazonSQS,
    private val url: String,
    private val mapper: ValueMapper<Message>,
    private val pollWaitTime: Duration,
    private val deliveryDelay: Duration?,
): WorkQueue<Message> {

    companion object {
        private const val maxReceiveCount = 10  // SQS has a limit to the number of messages to receive
    }

    override fun invoke(maxMessages: Int): List<SqsV1QueueItem<Message>> {
        val request = ReceiveMessageRequest(url)
            .withMaxNumberOfMessages(maxMessages.coerceAtMost(maxReceiveCount))
            .withWaitTimeSeconds(pollWaitTime.seconds.toInt())

        return sqs.receiveMessage(request).messages.mapNotNull { message ->
            SqsV1QueueItem(
                messageId = message.messageId,
                message = mapper.read(message.body) ?: return@mapNotNull null,
                receiptHandle = message.receiptHandle
            )
        }
    }

    override fun plusAssign(message: Message) {
        val request = SendMessageRequest()
            .withQueueUrl(url)
            .withMessageBody(mapper.write(message))
            .withDelaySeconds(deliveryDelay?.seconds?.toInt())

        sqs.sendMessage(request)
    }

    override fun minusAssign(items: Collection<QueueItem<Message>>) {
        val entries = items.filterIsInstance<SqsV1QueueItem<Message>>().map { item ->
            DeleteMessageBatchRequestEntry()
                .withId(item.messageId)
                .withReceiptHandle(item.receiptHandle)
        }

        if (entries.isEmpty()) return

        sqs.deleteMessageBatch(url, entries)
    }

    override fun setTimeout(item: QueueItem<Message>, duration: Duration) {
        if (item !is SqsV1QueueItem<Message>) return

        sqs.changeMessageVisibility(url, item.receiptHandle, duration.seconds.toInt())
    }

    override fun toString() = "${javaClass.simpleName}: $url"
}

data class SqsV1QueueItem<Message>(
    val messageId: String,
    override val message: Message,
    val receiptHandle: String
): QueueItem<Message>
