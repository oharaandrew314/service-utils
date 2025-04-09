package dev.andrewohara.utils.retry

import org.http4k.format.Json
import org.http4k.lens.BiDiMapping
import java.time.Duration
import redis.clients.jedis.JedisPool
import java.time.Instant

private const val ATTEMPT = "attempts"
private const val NEXT_ATTEMPT = "nextAttempt"

class JedisRetryStorage<ID: Any, NODE: Any>(
    private val pool: JedisPool,
    private val idMapping: BiDiMapping<ID, String>,
    private val json: Json<NODE>,
    private val retention: Duration = Duration.ofDays(1)
): RetryStorage<ID> {

    override fun get(id: ID): RetryLimiterData? {
        val data = pool.resource
            .use { it.get(idMapping(id)) ?: return null }
            .let { json.parse(it) }

        val props = with(json) {
            fields(data).associate { (key, value) -> key to text(value) }
        }

        return RetryLimiterData(
            attempts = props.getValue(ATTEMPT).toInt(),
            nextAttempt = Instant.parse(props.getValue(NEXT_ATTEMPT))
        )
    }

    override fun set(id: ID, data: RetryLimiterData) {
        val serialized = with(json) {
            val element = obj(
                ATTEMPT to string(data.attempts.toString()),
                NEXT_ATTEMPT to string(data.nextAttempt.toString())
            )
            compact(element)
        }

        pool.resource.use {
            it.setex(idMapping(id), retention.toSeconds(), serialized)
        }
    }

    override fun minusAssign(id: ID) {
        pool.resource.use {
            it.del(idMapping(id))
        }
    }

}