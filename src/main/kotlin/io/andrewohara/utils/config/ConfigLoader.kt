package io.andrewohara.utils.config

import io.andrewohara.utils.mappers.ValueMapper
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

fun interface ConfigLoader<T>: (String) -> T? {
    infix fun or(fallback: ConfigLoader<T>) = ConfigLoader { name -> this(name) ?: fallback(name) }
    fun orThrow(name: String): T = this(name) ?: throw IllegalArgumentException("Could not find resource: $name")

    companion object
}

fun ConfigLoader.Companion.file(base: Path = Paths.get(".")) = ConfigLoader { name ->
    val path = base.resolve(name)
    if (Files.exists(path)) Files.readAllBytes(path) else null
}

fun ConfigLoader.Companion.resource(fromClassloader: Boolean = true) = ConfigLoader { name ->
    val stream = if (fromClassloader) {
        this::class.java.classLoader.getResourceAsStream(name)
    } else {
        this::class.java.getResourceAsStream(name)
    }

    stream?.readBytes()
}

fun ConfigLoader<ByteArray>.properties() = ConfigLoader { name ->
    this(name)?.inputStream()?.use {
        Properties().apply {
            load(it)
        }
    }
}

fun <T> ConfigLoader<ByteArray>.mapper(mapper: ValueMapper<T>) = ConfigLoader { name ->
    this(name)?.let { mapper.read(it) }
}

fun ConfigLoader<ByteArray>.string(charset: Charset = Charsets.UTF_8) = ConfigLoader { name ->
    this(name)?.toString(charset)
}

fun ConfigLoader.Companion.env() = ConfigLoader { name ->
    System.getenv(name)
}