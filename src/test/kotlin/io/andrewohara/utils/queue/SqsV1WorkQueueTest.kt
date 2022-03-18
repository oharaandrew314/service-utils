package io.andrewohara.utils.queue

import io.andrewohara.awsmock.sqs.MockSqsV1
import io.andrewohara.awsmock.sqs.backend.MockSqsBackend
import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.string
import java.time.Clock
import java.time.Duration

class SqsV1WorkQueueTest: AbstractWorkQueueTest<SqsV1QueueItem<String>>() {

    override fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String, SqsV1QueueItem<String>> {
        val sqs = MockSqsBackend(clock)
        val sqsQueue = sqs.create("work", mapOf(
            "VisibilityTimeout" to lockFor.seconds.toString()
        ))!!
        return WorkQueue.sqsV1(sqs = MockSqsV1(sqs), url = sqsQueue.url, mapper = ValueMapper.string())
    }
}