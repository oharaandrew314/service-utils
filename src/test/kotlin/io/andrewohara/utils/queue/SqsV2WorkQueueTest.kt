package io.andrewohara.utils.queue

import org.http4k.aws.AwsSdkClient
import org.http4k.connect.amazon.sqs.FakeSQS
import org.http4k.format.Jackson
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import java.time.Clock
import java.time.Duration

class SqsV2WorkQueueTest: AbstractWorkQueueTest<SqsV2QueueItem<String>>() {

    override fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String> {
        val sqs = SqsClient.builder()
            .httpClient(AwsSdkClient(FakeSQS()))
            .credentialsProvider { AwsBasicCredentials.create("id", "secret") }
            .region(Region.CA_CENTRAL_1)
            .build()

        val sqsQueue = sqs.createQueue {
            it.queueName("work")
        }

        return WorkQueue.sqsV2(sqs = sqs, url = sqsQueue.queueUrl(), marshaller = Jackson)
    }
}