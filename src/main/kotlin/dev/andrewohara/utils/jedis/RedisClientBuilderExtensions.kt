package dev.andrewohara.utils.jedis

import org.http4k.core.Uri
import redis.clients.jedis.DefaultJedisClientConfig
import redis.clients.jedis.RedisClient
import redis.clients.jedis.SslOptions
import redis.clients.jedis.SslVerifyMode
import redis.clients.jedis.util.JedisURIHelper
import java.net.URI

fun RedisClient.Builder.uri(uri: Uri) = this
    .hostAndPort(JedisURIHelper.getHostAndPort(URI.create(uri.toString())))
    .clientConfig(DefaultJedisClientConfig.builder().apply {
        if (uri.scheme == "rediss") {
            sslOptions(SslOptions.builder()
                .sslVerifyMode(if (uri.host == "localhost") SslVerifyMode.CA else SslVerifyMode.FULL)
                .build())
        }
    }.build())