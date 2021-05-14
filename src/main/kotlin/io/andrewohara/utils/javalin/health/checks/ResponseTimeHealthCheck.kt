package io.andrewohara.utils.javalin.health.checks

import io.javalin.Javalin
import java.util.*

class ResponseTimeHealthCheck(private val historySize: Int, private val requiredAverageResponseTime: Int): HealthCheck {

    companion object {
        private const val PROP_NAME = "ResponseTimeHealthCheck-start"

        fun create(app: Javalin, historySize: Int, requiredAverageResponseTime: Int): ResponseTimeHealthCheck {
            val healthCheck = ResponseTimeHealthCheck(historySize, requiredAverageResponseTime)

            app.before {
                it.attribute(PROP_NAME, System.currentTimeMillis())
            }

            app.after { context ->
                val before = context.attribute<Long>(PROP_NAME)!!
                val responseTime = System.currentTimeMillis() - before
                healthCheck.addSample(responseTime)
            }

            return healthCheck
        }
    }

    private val history: Queue<Long> = ArrayDeque(historySize)

    init {
        require(requiredAverageResponseTime > 0) { "Average Response Time must > 0, was $requiredAverageResponseTime"}
    }

    fun addSample(responseTime: Long) {
        synchronized(history) {
            while(history.size >= historySize) {
                history.remove()
            }

            history.add(responseTime)
        }
    }

    override fun isHealthy(): Boolean {
        val average = synchronized(history) {
            if (history.isEmpty()) return true

            history.average()
        }

        return average < requiredAverageResponseTime
    }
}