package io.andrewohara.utils.mappers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.InputStream
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.queue.QueueMessageMapper

val jacksonYaml = YAMLMapper().apply {
    registerModule(KotlinModule())
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

inline fun <reified T> ConfigLoader<ByteArray>.jacksonYaml(mapper: YAMLMapper = jacksonYaml) = ConfigLoader { name ->
    this(name)?.inputStream()?.use {
        mapper.readValue(it, T::class.java)
    }
}

inline fun <reified T> ConfigLoader<String>.jacksonYamlString(mapper: YAMLMapper = jacksonYaml) = ConfigLoader { name ->
    this(name)?.let {
        mapper.readValue(it, T::class.java)
    }
}

inline fun <reified T> ConfigLoader<InputStream>.jacksonYamlStream(mapper: YAMLMapper = jacksonYaml) = ConfigLoader { name ->
    this(name)?.use {
        mapper.readValue(it, T::class.java)
    }
}

inline fun <reified Message> QueueMessageMapper.Companion.jacksonYaml(mapper: YAMLMapper = jacksonYaml) = object: QueueMessageMapper<Message> {
    override fun toQueueBody(message: Message) = mapper.writeValueAsString(message)
    override fun toMessage(body: String) = mapper.readValue(body, Message::class.java)
}