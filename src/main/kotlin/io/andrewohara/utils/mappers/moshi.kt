package io.andrewohara.utils.mappers

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.InputStream
import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper
import okio.buffer
import okio.source
import java.io.Reader

val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

inline fun <reified T> ValueMapper.Companion.moshi(mapper: Moshi = moshi) = object: ValueMapper<T> {
    val adapter = mapper.adapter(T::class.java)

    override fun read(reader: Reader) = adapter.fromJson(reader.readText())
    override fun read(source: String) = adapter.fromJson(source)!!
    override fun read(input: InputStream) = adapter.fromJson(input.source().buffer())!!

    override fun write(value: T) = adapter.toJson(value)
}

inline fun <reified T> ConfigLoader<ByteArray>.moshi(mapper: Moshi = moshi) = mapper(ValueMapper.moshi<T>(mapper))