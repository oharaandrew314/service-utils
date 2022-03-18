package io.andrewohara.utils.queue

import io.andrewohara.utils.mappers.ValueMapper
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry
import java.time.Duration

fun <Message> WorkQueue.Companion.sqsV2(
    sqs: SqsClient,
    url: String,
    mapper: ValueMapper<Message>,
    pollWaitTime: Duration = Duration.ofSeconds(20),
    deliveryDelay: Duration? =  null,
) = SqsV2WorkQueue(
    sqs = sqs,
    url = url,
    mapper = mapper,
    pollWaitTime = pollWaitTime,
    deliveryDelay = deliveryDelay
)

class SqsV2WorkQueue<Message>(
    private val sqs: SqsClient,
    private val url: String,
    private val mapper: ValueMapper<Message>,
    private val pollWaitTime: Duration,
    private val deliveryDelay: Duration?,
): WorkQueue<Message, SqsV2QueueItem<Message>> {

    companion object {
        private const val maxReceiveCount = 10  // SQS has a limit to the number of messages to receive
    }

    override fun invoke(maxMessages: Int): List<SqsV2QueueItem<Message>> {
        val response = sqs.receiveMessage {
            it.queueUrl(url)
            it.maxNumberOfMessages(maxMessages.coerceAtMost(maxReceiveCount))
            it.waitTimeSeconds(pollWaitTime.seconds.toInt())
        }

        return response.messages().mapNotNull { message ->
            SqsV2QueueItem(
                messageId = message.messageId(),
                message = mapper.read(message.body()) ?: return@mapNotNull null,
                receiptHandle = message.receiptHandle()
            )
        }
    }

    override fun minusAssign(items: Collection<SqsV2QueueItem<Message>>) {
        sqs.deleteMessageBatch {
            it.queueUrl(url)
            it.entries(
                items.map { item ->
                    DeleteMessageBatchRequestEntry.builder()
                        .id(item.messageId)
                        .receiptHandle(item.receiptHandle)
                        .build()
                }
            )
        }
    }

    override fun plusAssign(message: Message) {
        sqs.sendMessage {
            it.queueUrl(url)
            it.messageBody(mapper.write(message))
            it.delaySeconds(deliveryDelay?.seconds?.toInt())
        }
    }

    override fun setTimeout(item: SqsV2QueueItem<Message>, duration: Duration) {
        sqs.changeMessageVisibility {
            it.queueUrl(url)
            it.receiptHandle(item.receiptHandle)
            it.visibilityTimeout(duration.seconds.toInt())
        }
    }

    override fun toString() = "${javaClass.simpleName}: $url"
}

data class SqsV2QueueItem<Message>(
    val messageId: String,
    override val message: Message,
    val receiptHandle: String
): QueueItem<Message>