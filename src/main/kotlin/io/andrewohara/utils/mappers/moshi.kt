package io.andrewohara.utils.mappers

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStream
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper as configMapper
import okio.buffer
import okio.source
import java.io.Reader


fun defaultMoshi(): Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

class MoshiValueMapper<T>(mapper: Moshi = defaultMoshi(), type: Class<T>): ValueMapper<T> {
    private val adapter = mapper.adapter(type)

    override fun read(reader: Reader) = adapter.fromJson(reader.readText())
    override fun read(source: String) = adapter.fromJson(source)!!
    override fun read(input: InputStream) = adapter.fromJson(input.source().buffer())!!

    override fun write(value: T): String = adapter.toJson(value)
}

inline fun <reified T> ValueMapper.Companion.moshi(mapper: Moshi = defaultMoshi()) = MoshiValueMapper(mapper, T::class.java)

inline fun <reified T> ConfigLoader<ByteArray>.moshi(mapper: Moshi = defaultMoshi()) = configMapper(ValueMapper.moshi<T>(mapper))
inline fun <reified T> ConfigLoader<ByteArray>.moshi(consumer: (Moshi) -> Moshi): ConfigLoader<T> {
    val mapper = consumer(defaultMoshi())
    return configMapper(ValueMapper.moshi(mapper))
}