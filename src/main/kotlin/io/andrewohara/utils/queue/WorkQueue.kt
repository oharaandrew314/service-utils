package io.andrewohara.utils.queue

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue

typealias Task<Message> = (QueueItem<Message>) -> Any

interface WorkQueue<Message> {
    fun send(message: Message)
    fun poll(maxMessages: Int): List<QueueItem<Message>>

    companion object {
        fun <Message> memoryThreadSafe(clock: Clock = Clock.systemUTC(), lockFor: Duration = Duration.ofSeconds(10)) = object: WorkQueue<Message> {
            val queue = ConcurrentLinkedQueue<Message>()
            val locks = mutableMapOf<Message, Instant>()

            private fun Message.visible(): Boolean {
                val releaseTime = locks[this] ?: return true
                return clock.instant() >= releaseTime
            }

            override fun send(message: Message) {
                queue += message
            }

            override fun poll(maxMessages: Int): List<QueueItem<Message>> {
                return queue.asSequence()
                    .filter { it.visible() }
                    .take(maxMessages)
                    .map { message ->
                        locks[message] = clock.instant() + lockFor

                        object: QueueItem<Message> {
                            override val message = message
                            override fun delete() {
                                queue.remove(message)
                                locks.remove(message)
                            }
                            override fun extendLock(duration: Duration) {
                                locks[message] = clock.instant() + lockFor
                            }
                        }
                    }.toList()
            }
        }
    }
}

interface QueueItem<Message> {
    val message: Message
    fun delete()
    fun extendLock(duration: Duration)
}

interface ExecutorHandle {
    fun stop(timeout: Duration? = null)
}