package io.andrewohara.utils.mappers

import com.google.gson.Gson
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper as configMapper
import java.io.Reader

class GsonValueMapper<T>(mapper: Gson = Gson(), type: Class<T>): ValueMapper<T> {
    private val adapter = mapper.getAdapter(type)

    override fun read(reader: Reader) = adapter.fromJson(reader)
    override fun read(source: String) = adapter.fromJson(source)
    override fun write(value: T) = adapter.toJson(value)
}

inline fun <reified T> ValueMapper.Companion.gson(mapper: Gson = Gson()) = GsonValueMapper(mapper, T::class.java)

inline fun <reified T> ConfigLoader<ByteArray>.gson(mapper: Gson = Gson()) = configMapper(ValueMapper.gson<T>(mapper))
inline fun <reified T> ConfigLoader<ByteArray>.gson(consumer: (Gson) -> Gson): ConfigLoader<T> {
    val mapper = consumer(Gson())
    return configMapper(ValueMapper.gson(mapper))
}