package io.andrewohara.utils.mappers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper as configMapper
import java.io.InputStream
import java.io.Reader

fun defaultJacksonYaml() = YAMLMapper().apply {
    registerModule(KotlinModule())
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

class JacksonYamlValueMapper<T>(private val mapper: YAMLMapper = defaultJacksonYaml(), private val type: Class<T>): ValueMapper<T> {
    override fun read(reader: Reader) = mapper.readValue(reader, type)
    override fun read(input: InputStream) = mapper.readValue(input, type)
    override fun read(source: ByteArray) = mapper.readValue(source, type)
    override fun read(source: String) = mapper.readValue(source, type)

    override fun write(value: T) = mapper.writeValueAsString(value)
}

inline fun <reified T> ValueMapper.Companion.jacksonYaml(mapper: YAMLMapper = defaultJacksonYaml()) = JacksonYamlValueMapper(mapper, T::class.java)

inline fun <reified T> ConfigLoader<ByteArray>.jacksonYaml(mapper: YAMLMapper = defaultJacksonYaml()) = configMapper(ValueMapper.jacksonYaml<T>(mapper))
inline fun <reified T> ConfigLoader<ByteArray>.jacksonYaml(consumer: (YAMLMapper) -> YAMLMapper): ConfigLoader<T> {
    val mapper = consumer(defaultJacksonYaml())
    return configMapper(ValueMapper.jacksonYaml(mapper))
}