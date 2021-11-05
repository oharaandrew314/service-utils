package io.andrewohara.utils.mappers

import com.google.gson.Gson
import java.io.InputStream
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.queue.QueueMessageMapper


inline fun <reified T> ConfigLoader<ByteArray>.gson(mapper: Gson = Gson()) = ConfigLoader { name ->
    this(name)?.inputStream()?.reader()?.use {
        mapper.fromJson(it, T::class.java)
    }
}

inline fun <reified T> ConfigLoader<InputStream>.gsonStream(mapper: Gson = Gson()) = ConfigLoader { name ->
    this(name)?.reader()?.use {
        mapper.fromJson(it, T::class.java)
    }
}

inline fun <reified T> ConfigLoader<String>.gsonString(mapper: Gson = Gson()) = ConfigLoader { name ->
    this(name)?.let {
        mapper.fromJson(it, T::class.java)
    }
}

inline fun <reified Message> QueueMessageMapper.Companion.gson(mapper: Gson = Gson()) = object: QueueMessageMapper<Message> {
    override fun toQueueBody(message: Message) = mapper.toJson(message)
    override fun toMessage(body: String) = mapper.fromJson(body, Message::class.java)
}