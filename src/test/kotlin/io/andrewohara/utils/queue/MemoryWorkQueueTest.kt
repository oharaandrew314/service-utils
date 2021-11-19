package io.andrewohara.utils.queue

import java.time.Clock
import java.time.Duration

class MemoryWorkQueueTest: AbstractWorkQueueTest() {

    override fun createQueue(clock: Clock, lockFor: Duration): WorkQueue<String> {
        return WorkQueue.memoryThreadSafe(clock, lockFor = Duration.ofSeconds(10))
    }
}