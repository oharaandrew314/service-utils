package io.andrewohara.utils.queue

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
import com.amazonaws.services.sqs.model.SendMessageRequest
import org.slf4j.LoggerFactory
import java.time.Duration

class SqsV1Queue<Message>(
    private val sqs: AmazonSQS,
    private val queueUrl: String,
    private val messageMapper: QueueMessageMapper<Message>,
    private val pollWaitTime: Duration? = null,
    private val deliveryDelay: Duration? =  null,
): Queue<Message> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun poll(maxMessages: Int): List<QueueMessage<Message>> {
        val request = ReceiveMessageRequest(queueUrl)
            .withMaxNumberOfMessages(maxMessages.coerceAtMost(10)) // SQS has a limit to the number of messages to receive
            .withWaitTimeSeconds(pollWaitTime?.seconds?.toInt())

        return sqs.receiveMessage(request).messages.map { message ->
            SqsQueueItem(
                messageId = message.messageId,
                body = messageMapper.toMessage(message.body),
                receiptHandle = message.receiptHandle
            )
        }
    }

    override fun send(message: Message) {
        val request = SendMessageRequest()
            .withQueueUrl(queueUrl)
            .withMessageBody(messageMapper.toQueueBody(message))
            .withDelaySeconds(deliveryDelay?.seconds?.toInt())

        val response = sqs.sendMessage(request)
        log.debug("Sent message to $queueUrl: ${response.messageId}")
    }

    override fun toString() = "${javaClass.simpleName}: $queueUrl"

    inner class SqsQueueItem(
        private val messageId: String,
        override val body: Message,
        private val receiptHandle: String
    ): QueueMessage<Message> {
        override fun delete() {
            sqs.deleteMessage(queueUrl, receiptHandle)
        }

        override fun delayRetry(duration: Duration) {
            sqs.changeMessageVisibility(queueUrl, receiptHandle, duration.toSeconds().toInt())
        }

        override fun toString() = messageId
    }
}