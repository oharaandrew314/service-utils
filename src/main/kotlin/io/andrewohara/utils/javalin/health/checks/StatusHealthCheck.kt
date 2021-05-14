package io.andrewohara.utils.javalin.health.checks

import io.javalin.Javalin
import java.util.*

class StatusHealthCheck(
    private val historySize: Int,
    private val requiredSuccessRatio: Float
): HealthCheck {

    companion object {
        fun create(app: Javalin, historySize: Int, requiredSuccessRatio: Float): HealthCheck {
            val healthCheck = StatusHealthCheck(historySize, requiredSuccessRatio)
            app.after {
                healthCheck.addSample(it.status())
            }
            return healthCheck
        }
    }

    private val history: Queue<Int> = ArrayDeque(historySize)

    init {
        require(requiredSuccessRatio > 0 && requiredSuccessRatio <= 1) { "Success Ratio must be (0, 1], was $requiredSuccessRatio"}
    }

    fun addSample(status: Int) {
        synchronized(history) {
            while(history.size >= historySize) {
                history.remove()
            }

            history.add(status)
        }
    }

    override fun isHealthy(): Boolean {
        val ratio = synchronized(history) {
            if (history.isEmpty()) return true

            val healthy = history.filter { it.isHealthy() }.count()
            healthy.toFloat() / history.size
        }

        return ratio >= requiredSuccessRatio
    }

    private fun Int.isHealthy() = this < 500
}