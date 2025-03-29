package dev.andrewohara.utils.redis

import com.github.fppt.jedismock.RedisServer
import io.kotest.matchers.shouldBe
import org.http4k.base64Encode
import org.http4k.config.Host
import org.http4k.config.Port
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.format.Moshi
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveStatus
import org.http4k.routing.bind
import org.http4k.routing.body
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.ServerConfig
import org.http4k.server.asServer
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.redisson.Redisson
import redis.clients.jedis.JedisPool
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import kotlin.random.Random

abstract class HttpOverRedisContract<Client: Any>(
    getRedis: (Port) -> Client,
    getClient: (Client) -> HttpHandler,
    getServer: (Client, Host) -> ServerConfig,
) {

//    private val redisServer = RedisServer.newRedisServer().start()
//        .also { println("Redis started on port ${it.bindPort}") }
    private val redisClient = getRedis(Port(6379))

    private val host = Host("app1")

    @OptIn(ExperimentalStdlibApi::class)
    private val httpServer = routes(
        "/body" bind Method.POST to {
            val content = it.body.stream.readAllBytes()
            println("Server received ${content.toHexString()}")
            Response(Status.OK).body(MemoryBody(content))
        },
        "/hello/{name}" bind Method.GET to {
            Response(Status.OK).body(it.path("name")!!)
        }
    )
        .asServer(getServer(redisClient, host))
        .also { it.start() }

    private val httpClient = ClientFilters.SetHostFrom(Uri.of("http://${host.value}"))
        .then(getClient(redisClient))

    @Test
    fun `GET request with response body`() = repeat(10) {
        println("GET $it")
        val response = Request(Method.GET, "/hello/bob").let(httpClient)
        response shouldHaveStatus Status.OK
        response shouldHaveBody "bob"
    }

    @OptIn(ExperimentalStdlibApi::class)
//    @Test
    fun `POST request with large binary body`() = repeat(10) {
        println("POST $it")
        val content = Random(1337).nextBytes(8)
        println("Generated ${content.toHexString()}")

        val response = Request(Method.POST, "/body")
            .body(MemoryBody(content))
            .let(httpClient)

        response shouldHaveStatus Status.OK
        val received = response.body.stream.readAllBytes().toHexString()
        println("Got back $received")
        response.body.stream.readAllBytes().toHexString() shouldBe content.toHexString()
    }
}

class HttpOverJedisTest: HttpOverRedisContract<JedisPool>(
    getRedis = { JedisPool("localhost", it.value) },
    getClient = { HttpOverRedis.jedisClient(it, Moshi) },
    getServer = { jedis, host -> HttpOverRedis.jedisServer(jedis, host, Moshi) }
)

class HttpOverRedissonTest: HttpOverRedisContract<RedissonClient>(
    getRedis = {
        Config()
            .apply { useSingleServer().setAddress("redis://localhost:${it.value}") }
            .let { Redisson.create(it) }
    },
    getClient = { HttpOverRedis.redissonClient(it, Moshi) },
    getServer = { client, host -> HttpOverRedis.redissonServer(client, host, Moshi) }
)