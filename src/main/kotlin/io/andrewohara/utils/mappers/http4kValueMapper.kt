package io.andrewohara.utils.mappers

import io.andrewohara.utils.config.ConfigLoader
import io.andrewohara.utils.config.mapper
import org.http4k.format.AutoMarshalling
import java.io.InputStream
import java.io.Reader
import kotlin.reflect.KClass

class Http4kValueMapper<T: Any>(private val marshaller: AutoMarshalling, private val type: KClass<T>): ValueMapper<T> {

    override fun read(input: InputStream) = marshaller.asA(input, type)
    override fun read(reader: Reader) = marshaller.asA(reader.readText(), type)
    override fun read(source: String) = marshaller.asA(source, type)

    override fun write(value: T) = marshaller.asFormatString(value)
}

inline fun <reified T: Any> ValueMapper.Companion.http4k(marshaller: AutoMarshalling) = Http4kValueMapper(marshaller, T::class)
inline fun <reified T: Any> ConfigLoader<ByteArray>.http4k(marshaller: AutoMarshalling): ConfigLoader<T> = mapper(ValueMapper.http4k(marshaller))