package io.andrewohara.utils.mappers

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper
import java.io.InputStream
import java.io.Reader

val jacksonYaml = YAMLMapper().apply {
    registerModule(KotlinModule())
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}

inline fun <reified T> ValueMapper.Companion.jacksonYaml(mapper: YAMLMapper = jacksonYaml) = object: ValueMapper<T> {
    override fun read(reader: Reader) = mapper.readValue(reader, T::class.java)
    override fun read(input: InputStream) = mapper.readValue(input, T::class.java)
    override fun read(source: ByteArray) = mapper.readValue(source, T::class.java)
    override fun read(source: String) = mapper.readValue(source, T::class.java)

    override fun write(value: T) = mapper.writeValueAsString(value)
}

inline fun <reified T> ConfigLoader<ByteArray>.jacksonYaml(mapper: YAMLMapper = jacksonYaml) = mapper(ValueMapper.jacksonYaml<T>(mapper))