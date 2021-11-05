package io.andrewohara.utils.mappers

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStream
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.queue.QueueMessageMapper
import okio.buffer
import okio.source

val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

inline fun <reified T> ConfigLoader<ByteArray>.moshi(
    mapper: JsonAdapter<T> = moshi.adapter(T::class.java)
) = ConfigLoader { name ->
    this(name)?.inputStream()?.source()?.buffer()?.use {
        mapper.fromJson(it)
    }
}

inline fun <reified T> ConfigLoader<String>.moshiString(
    mapper: JsonAdapter<T> = moshi.adapter(T::class.java)
) = ConfigLoader { name ->
    this(name)?.let {
        mapper.fromJson(it)
    }
}

inline fun <reified T> ConfigLoader<InputStream>.moshiStream(
    mapper: JsonAdapter<T> = moshi.adapter(T::class.java)
) = ConfigLoader { name ->
    this(name)?.source()?.buffer()?.use {
        mapper.fromJson(it)
    }
}

inline fun <reified Message> QueueMessageMapper.Companion.moshi(
    mapper: JsonAdapter<Message> = moshi.adapter(Message::class.java)
) = object: QueueMessageMapper<Message> {
    override fun toQueueBody(message: Message) = mapper.toJson(message)
    override fun toMessage(body: String) = mapper.fromJson(body)!!
}