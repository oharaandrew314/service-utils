package io.andrewohara.utils.mappers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.queue.QueueMessageMapper
import java.io.InputStream

val jacksonJson: ObjectMapper = jacksonObjectMapper()
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

inline fun <reified T> ConfigLoader<ByteArray>.jacksonJson(mapper: ObjectMapper = jacksonJson) = ConfigLoader { name ->
    this(name)?.inputStream()?.use {
        mapper.readValue(it, T::class.java)
    }
}

inline fun <reified T> ConfigLoader<String>.jacksonJsonString(mapper: ObjectMapper = jacksonJson) = ConfigLoader { name ->
    this(name)?.let {
        mapper.readValue(it, T::class.java)
    }
}

inline fun <reified T> ConfigLoader<InputStream>.jacksonJsonStream(mapper: ObjectMapper = jacksonJson) = ConfigLoader { name ->
    this(name)?.use {
        mapper.readValue(it, T::class.java)
    }
}

inline fun <reified Message> QueueMessageMapper.Companion.jacksonJson(
    mapper: ObjectMapper = jacksonJson
) = object: QueueMessageMapper<Message> {
    override fun toQueueBody(message: Message) = mapper.writeValueAsString(message)
    override fun toMessage(body: String) = mapper.readValue(body, Message::class.java)
}