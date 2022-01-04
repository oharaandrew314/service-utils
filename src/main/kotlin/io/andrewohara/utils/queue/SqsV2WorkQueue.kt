package io.andrewohara.utils.queue

import io.andrewohara.utils.mappers.ValueMapper
import software.amazon.awssdk.services.sqs.SqsClient
import java.time.Duration

class SqsV2WorkQueue<Message>(
    private val sqs: SqsClient,
    private val url: String,
    private val mapper: ValueMapper<Message>,
    private val pollWaitTime: Duration,
    private val deliveryDelay: Duration?,
): WorkQueue<Message> {

    companion object {
        private const val maxReceiveCount = 10  // SQS has a limit to the number of messages to receive
        fun <Message> WorkQueue.Companion.sqsV2(
            sqs: SqsClient,
            url: String,
            mapper: ValueMapper<Message>,
            pollWaitTime: Duration = Duration.ofSeconds(10),
            deliveryDelay: Duration? =  null,
        ) = SqsV2WorkQueue(
            sqs = sqs,
            url = url,
            mapper = mapper,
            pollWaitTime = pollWaitTime,
            deliveryDelay = deliveryDelay
        )
    }

    override fun poll(maxMessages: Int): List<QueueItem<Message>> {
        val response = sqs.receiveMessage {
            it.queueUrl(url)
            it.maxNumberOfMessages(maxMessages.coerceAtMost(maxReceiveCount))
            it.waitTimeSeconds(pollWaitTime.seconds.toInt())
        }

        return response.messages().mapNotNull { message ->
            SqsQueueItem(
                messageId = message.messageId(),
                message = mapper.read(message.body()) ?: return@mapNotNull null,
                receiptHandle = message.receiptHandle()
            )
        }
    }

    override fun send(message: Message) {
        sqs.sendMessage {
            it.queueUrl(url)
            it.messageBody(mapper.write(message))
            it.delaySeconds(deliveryDelay?.seconds?.toInt())
        }
    }

    override fun toString() = "${javaClass.simpleName}: $url"

    inner class SqsQueueItem(
        private val messageId: String,
        override val message: Message,
        private val receiptHandle: String
    ): QueueItem<Message> {
        override fun delete() {
            sqs.deleteMessage {
                it.queueUrl(url)
                it.receiptHandle(receiptHandle)
            }
        }

        override fun extendLock(duration: Duration) {
            sqs.changeMessageVisibility {
                it.queueUrl(url)
                it.receiptHandle(receiptHandle)
                it.visibilityTimeout(duration.seconds.toInt())
            }
        }

        override fun toString() = messageId
    }
}
