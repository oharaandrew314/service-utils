package io.andrewohara.utils.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import io.andrewohara.utils.mappers.ValueMapper
import java.time.Duration

class SqsV1Queue<Message>(
    private val sqs: AmazonSQS,
    private val queueUrl: String,
    private val mapper: ValueMapper<Message>,
    private val pollWaitTime: Duration? = null,
    private val deliveryDelay: Duration? =  null,
): Queue<Message> {

    override fun poll(maxMessages: Int): List<QueueItem<Message>> {
        val request = ReceiveMessageRequest(queueUrl)
            .withMaxNumberOfMessages(maxMessages.coerceAtMost(10)) // SQS has a limit to the number of messages to receive
            .withWaitTimeSeconds(pollWaitTime?.seconds?.toInt())

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
            .withQueueUrl(queueUrl)
            .withMessageBody(mapper.write(message))
            .withDelaySeconds(deliveryDelay?.seconds?.toInt())

        sqs.sendMessage(request)
    }

    override fun toString() = "${javaClass.simpleName}: $queueUrl"

    inner class SqsQueueItem(
        private val messageId: String,
        override val message: Message,
        private val receiptHandle: String
    ): QueueItem<Message> {
        override fun delete() {
            sqs.deleteMessage(queueUrl, receiptHandle)
        }

        override fun extendLock(duration: Duration) {
            sqs.changeMessageVisibility(queueUrl, receiptHandle, duration.toSeconds().toInt())
        }

        override fun toString() = messageId
    }
}
