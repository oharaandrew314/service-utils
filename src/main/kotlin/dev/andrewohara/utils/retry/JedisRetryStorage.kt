package dev.andrewohara.utils.retry

import org.http4k.format.Json
import org.http4k.lens.BiDiMapping
import java.time.Duration
import redis.clients.jedis.UnifiedJedis
import redis.clients.jedis.params.SetParams
import java.time.Instant

private const val ATTEMPT = "attempts"
private const val NEXT_ATTEMPT = "nextAttempt"

class JedisRetryStorage<ID: Any, NODE: Any>(
    private val pool: UnifiedJedis,
    private val idMapping: BiDiMapping<ID, String>,
    private val json: Json<NODE>,
    private val retention: Duration = Duration.ofDays(1)
): RetryStorage<ID> {

    override fun get(id: ID): RetryLimiterData? {
        val data = pool
            .get(idMapping(id))
            ?.let { json.parse(it) }
            ?: return null

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

        pool.set(idMapping(id), serialized, SetParams().ex(retention.toSeconds()))
    }

    override fun minusAssign(id: ID) {
        pool.del(idMapping(id))
    }
}