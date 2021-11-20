package io.andrewohara.utils.mappers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper as configMapper
import java.io.InputStream
import java.io.Reader

fun defaultJacksonJson(): JsonMapper = JsonMapper().apply {
    registerModule(KotlinModule())
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

class JacksonJsonValueMapper<T>(private val mapper: JsonMapper = defaultJacksonJson(), private val type: Class<T>): ValueMapper<T> {
    override fun read(reader: Reader): T = mapper.readValue(reader, type)
    override fun read(input: InputStream): T = mapper.readValue(input, type)
    override fun read(source: String): T = mapper.readValue(source, type)
    override fun read(source: ByteArray): T = mapper.readValue(source, type)
    override fun write(value: T): String = mapper.writeValueAsString(value)
}

inline fun <reified T> ValueMapper.Companion.jacksonJson(mapper: JsonMapper = defaultJacksonJson()) = JacksonJsonValueMapper(mapper, T::class.java)

inline fun <reified T> ConfigLoader<ByteArray>.jacksonJson(mapper: JsonMapper = defaultJacksonJson()) = configMapper(ValueMapper.jacksonJson<T>(mapper))
inline fun <reified T> ConfigLoader<ByteArray>.jacksonJson(consumer: (JsonMapper) -> JsonMapper): ConfigLoader<T> {
    val jacksonMapper = consumer(defaultJacksonJson())
    return configMapper(ValueMapper.jacksonJson(jacksonMapper))
}