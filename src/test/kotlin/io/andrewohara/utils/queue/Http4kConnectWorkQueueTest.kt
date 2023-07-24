package io.andrewohara.utils.queue

import dev.forkhandles.result4k.valueOrNull
import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.string
import org.http4k.connect.amazon.sqs.FakeSQS
import org.http4k.connect.amazon.sqs.createQueue
import org.http4k.connect.amazon.sqs.model.QueueName
import java.time.Clock
import java.time.Duration

class Http4kConnectWorkQueueTest: AbstractWorkQueueTest<Http4kConnectWorkQueueItem<String>>() {

    override fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String> {
        val sqs = FakeSQS().client()
        val queue = sqs.createQueue(QueueName.of("work"), emptyList(), emptyMap()).valueOrNull()!!

        return WorkQueue.http4k(sqs = sqs, url = queue.QueueUrl, mapper = ValueMapper.string())
    }
}