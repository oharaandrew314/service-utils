package io.andrewohara.utils.mappers

import com.google.gson.Gson
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper
import java.io.Reader

inline fun <reified T> ValueMapper.Companion.gson(mapper: Gson = Gson()) = object: ValueMapper<T> {
    val adapter = mapper.getAdapter(T::class.java)

    override fun read(reader: Reader) = adapter.fromJson(reader)
    override fun read(source: String) = adapter.fromJson(source)
    override fun write(value: T) = adapter.toJson(value)
}

inline fun <reified T> ConfigLoader<ByteArray>.gson(mapper: Gson = Gson()) = mapper(ValueMapper.gson<T>(mapper))