package io.andrewohara.utils.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import io.andrewohara.utils.mappers.ValueMapper
import java.time.Duration

class SqsV1WorkQueue<Message>(
    private val sqs: AmazonSQS,
    private val url: String,
    private val mapper: ValueMapper<Message>,
    private val pollWaitTime: Duration,
    private val deliveryDelay: Duration?,
): WorkQueue<Message> {

    companion object {
        private const val maxReceiveCount = 10  // SQS has a limit to the number of messages to receive
        fun <Message> WorkQueue.Companion.sqsV1(
            sqs: AmazonSQS,
            url: String,
            mapper: ValueMapper<Message>,
            pollWaitTime: Duration = Duration.ofSeconds(10),
            deliveryDelay: Duration? =  null,
        ) = SqsV1WorkQueue(
            sqs = sqs,
            url = url,
            mapper = mapper,
            pollWaitTime = pollWaitTime,
            deliveryDelay = deliveryDelay
        )
    }

    override fun poll(maxMessages: Int): List<QueueItem<Message>> {
        val request = ReceiveMessageRequest(url)
            .withMaxNumberOfMessages(maxMessages.coerceAtMost(maxReceiveCount))
            .withWaitTimeSeconds(pollWaitTime.seconds.toInt())

        return sqs.receiveMessage(request).messages.mapNotNull { message ->
            SqsQueueItem(
                messageId = message.messageId,
                message = mapper.read(message.body) ?: return@mapNotNull null,
                receiptHandle = message.receiptHandle
            )
        }
    }

    override fun send(message: Message) {
        val request = SendMessageRequest()
            .withQueueUrl(url)
            .withMessageBody(mapper.write(message))
            .withDelaySeconds(deliveryDelay?.seconds?.toInt())

        sqs.sendMessage(request)
    }

    override fun toString() = "${javaClass.simpleName}: $url"

    inner class SqsQueueItem(
        private val messageId: String,
        override val message: Message,
        private val receiptHandle: String
    ): QueueItem<Message> {
        override fun delete() {
            sqs.deleteMessage(url, receiptHandle)
        }

        override fun extendLock(duration: Duration) {
            sqs.changeMessageVisibility(url, receiptHandle, duration.seconds.toInt())
        }

        override fun toString() = messageId
    }
}
