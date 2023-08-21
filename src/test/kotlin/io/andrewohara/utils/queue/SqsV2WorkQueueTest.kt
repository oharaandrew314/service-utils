package io.andrewohara.utils.queue

import io.andrewohara.utils.mappers.ValueMapper
import io.andrewohara.utils.mappers.string
import org.http4k.aws.AwsSdkClient
import org.http4k.connect.amazon.sqs.FakeSQS
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.services.sqs.SqsClient
import java.time.Clock
import java.time.Duration

class SqsV2WorkQueueTest: AbstractWorkQueueTest<SqsV2QueueItem<String>>() {

    override fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String> {
        val sqs = SqsClient.builder()
            .httpClient(AwsSdkClient(FakeSQS()))
            .credentialsProvider { AwsBasicCredentials.create("id", "secret") }
            .build()

        val sqsQueue = sqs.createQueue {
            it.queueName("work")
        }

        return WorkQueue.sqsV2(sqs = sqs, url = sqsQueue.queueUrl(), mapper = ValueMapper.string())
    }
}