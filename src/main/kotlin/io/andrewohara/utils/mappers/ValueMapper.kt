package io.andrewohara.utils.mappers

import java.io.InputStream
import java.io.Reader

interface ValueMapper<T> {
    fun read(source: String) = source.reader().use { read(it) }
    fun read(source: ByteArray) = source.inputStream().use { read(it) }
    fun read(input: InputStream) = input.reader().use { read(it) }
    fun read(reader: Reader): T?

    fun write(value: T): String

    companion object {
        fun string() = object: ValueMapper<String> {
            override fun read(source: String) = source
            override fun read(reader: Reader) = reader.readText()
            override fun write(value: String) = value
        }
    }
}
