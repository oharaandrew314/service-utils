package dev.andrewohara.utils.redis

import org.http4k.config.Host
import org.http4k.core.*
import org.http4k.format.Json
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPubSub
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun <NODE> HttpOverRedis.jedisClient(
    pool: JedisPool,
    json: Json<NODE>,
    responseTimeout: Duration = Duration.ofSeconds(10),
    executor: ExecutorService = Executors.newVirtualThreadPerTaskExecutor()
): HttpHandler = { request ->
    val latch = CountDownLatch(1)
    var response: Response? = null

    val sub = object: JedisPubSub() {
        override fun onMessage(channel: String, message: String) {
            val parsed = RedisHttpMessage.parse(message, json)
            println("Client receive response from $channel")
            response = parsed.response
            latch.countDown()
        }
    }

    val message = RedisHttpMessage(request)

    val poller = executor.submit {
        pool.resource.use { it.subscribe(sub,message.requestId) }
    }

    pool.resource.use { jedis ->
        println("Client send to ${request.uri.host}")
        jedis.publish(request.uri.host, message.toJson(json))
    }

    latch.await(responseTimeout.toMillis(), TimeUnit.SECONDS)
    sub.unsubscribe()
    poller.cancel(true)

    response ?: Response(Status.REQUEST_TIMEOUT)
}

fun <NODE> HttpOverRedis.jedisServer(pool: JedisPool, host: Host, json: Json<NODE>) = object: ServerConfig {

    private var pollThread: Thread? = null

    override fun toServer(http: HttpHandler) = object: Http4kServer {
        override fun port() = -1

        override fun start(): Http4kServer {
            val sub = object: JedisPubSub() {
                override fun onSubscribe(channel: String, subscribedChannels: Int) {
                    println("${host.value} subscribed to $channel")
                }

                override fun onMessage(channel: String, message: String) {
                    val parsed = RedisHttpMessage.parse(message, json)
                    val response = http(parsed.request)

                    val responseMessage = RedisHttpMessage(response, parsed.requestId)
                    pool.resource.use { jedis ->
                        jedis.publish(parsed.requestId, responseMessage.toJson(json))
                    }
                }

                override fun onUnsubscribe(channel: String, subscribedChannels: Int) {
                    println("${host.value} unsubscribed from $channel")
                }
            }

            pollThread = Thread.startVirtualThread {
                pool.resource.subscribe(sub, host.value)
            }

            return this
        }

        override fun stop(): Http4kServer {
            pool.close()
            pollThread?.interrupt()
            return this
        }
    }
}