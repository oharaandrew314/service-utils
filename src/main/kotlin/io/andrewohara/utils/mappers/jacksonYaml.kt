package io.andrewohara.utils.mappers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper as configMapper
import java.io.InputStream
import java.io.Reader

object JacksonYaml {
    fun defaultMapper() = YAMLMapper().apply {
        registerModule(KotlinModule())
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }

    inline fun <reified T> ValueMapper.Companion.jacksonYaml(mapper: YAMLMapper = defaultMapper()) = object: ValueMapper<T> {
        override fun read(reader: Reader) = mapper.readValue(reader, T::class.java)
        override fun read(input: InputStream) = mapper.readValue(input, T::class.java)
        override fun read(source: ByteArray) = mapper.readValue(source, T::class.java)
        override fun read(source: String) = mapper.readValue(source, T::class.java)

        override fun write(value: T) = mapper.writeValueAsString(value)
    }

    inline fun <reified T> ConfigLoader<ByteArray>.jacksonYaml(mapper: YAMLMapper = defaultMapper()) = configMapper(ValueMapper.jacksonYaml<T>(mapper))
    inline fun <reified T> ConfigLoader<ByteArray>.jacksonYaml(consumer: (YAMLMapper) -> YAMLMapper): ConfigLoader<T> {
        val mapper = consumer(defaultMapper())
        return configMapper(ValueMapper.jacksonYaml(mapper))
    }
}