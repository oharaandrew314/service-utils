package dev.andrewohara.utils.redis

import org.http4k.config.Host
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.format.Json
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.redisson.api.RedissonClient
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun <NODE> HttpOverRedis.redissonClient(
    redisson: RedissonClient,
    json: Json<NODE>,
    responseTimeout: Duration = Duration.ofSeconds(10)
): HttpHandler = { request ->
    val message = RedisHttpMessage(request)
    val requestTopic = redisson.getTopic(request.uri.host)
    val responseTopic = redisson.getTopic(message.requestId)

    var response: Response? = null
    val latch = CountDownLatch(1)
    println("foo")
    val listenerId = responseTopic.addListener(String::class.java) { _, msg ->
        val parsed = RedisHttpMessage.parse(msg, json)
        println("Client received response $parsed")
        response = parsed.response
        latch.countDown()
    }

    println("Client sends request: $message")
    requestTopic.publish(message.toJson(json))
    latch.await(responseTimeout.toMillis(), TimeUnit.MILLISECONDS)
    responseTopic.removeListener(listenerId)

    response ?: Response(Status.REQUEST_TIMEOUT)
}

fun <NODE> HttpOverRedis.redissonServer(
    redisson: RedissonClient,
    host: Host,
    json: Json<NODE>
) = object: ServerConfig {
    override fun toServer(http: HttpHandler) = object: Http4kServer {
        private val topic = redisson.getTopic(host.value)
        private var listenerId: Int? = null

        override fun port() = -1

        override fun start(): Http4kServer {
            println("Start $host")
            listenerId = topic.addListener(String::class.java) { _, msg ->
                val parsed = RedisHttpMessage.parse(msg, json)
                println("$host received request $parsed")
                val response = http(parsed.request)

                val responseMessage = RedisHttpMessage(response, parsed.requestId)
                println("$host sent response $responseMessage")
                redisson.getTopic(parsed.requestId).publish(responseMessage.toJson(json))
            }

            return this
        }

        override fun stop(): Http4kServer {
            listenerId?.let {
                topic.removeListener(it)
            }
            return this
        }
    }
}