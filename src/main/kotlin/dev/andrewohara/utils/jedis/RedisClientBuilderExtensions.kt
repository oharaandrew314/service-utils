package dev.andrewohara.utils.jedis

import org.http4k.core.Uri
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.RedisClient
import redis.clients.jedis.SslOptions
import redis.clients.jedis.SslVerifyMode

private val localhosts get()= setOf("localhost", "127.0.0.1")

fun RedisClient.Builder.uri(uri: Uri) = this
    .hostAndPort(uri.host, uri.port ?: 6379)
    .clientConfig(DefaultJedisClientConfig.builder().apply {
        if (uri.scheme == "rediss") {
            sslOptions(SslOptions.builder()
                .sslVerifyMode(if (uri.host in localhosts) SslVerifyMode.CA else SslVerifyMode.FULL)
                .build())
        }
    }.build())