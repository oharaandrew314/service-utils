package io.andrewohara.utils.jdk

import java.util.concurrent.AbstractExecutorService
import java.util.concurrent.TimeUnit

class SameThreadExecutorService: AbstractExecutorService() {

    private var terminated = false

    override fun shutdown() {
        terminated = true
    }

    override fun isShutdown() = terminated
    override fun isTerminated() = terminated

    override fun awaitTermination(theTimeout: Long, theUnit: TimeUnit): Boolean {
        shutdown()
        return terminated
    }

    override fun shutdownNow(): List<Runnable> {
        return emptyList()
    }

    override fun execute(command: Runnable) {
        command.run()
    }
}