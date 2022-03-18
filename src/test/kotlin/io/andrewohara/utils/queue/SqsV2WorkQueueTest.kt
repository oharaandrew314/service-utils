package io.andrewohara.utils.queue

import io.andrewohara.awsmock.sqs.MockSqsV2
import io.andrewohara.awsmock.sqs.backend.MockSqsBackend
import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.string
import java.time.Clock
import java.time.Duration

class SqsV2WorkQueueTest: AbstractWorkQueueTest<SqsV2QueueItem<String>>() {

    override fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String, SqsV2QueueItem<String>> {
        val sqs = MockSqsBackend(clock)
        val sqsQueue = sqs.create("work", mapOf(
            "VisibilityTimeout" to lockFor.seconds.toString()
        ))!!
        return WorkQueue.sqsV2(sqs = MockSqsV2(sqs), url = sqsQueue.url, mapper = ValueMapper.string())
    }
}