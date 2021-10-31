package io.andrewohara.utils.config

import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

fun interface ConfigLoader<T>: () -> T? {
    companion object {
        fun file(path: Path) = ConfigLoader {
            if (Files.exists(path)) Files.readAllBytes(path) else null
        }
        fun resource(name: String) = ConfigLoader {
            this::class.java.classLoader.getResourceAsStream(name)?.readBytes()
        }

        fun ConfigLoader<ByteArray>.properties() = ConfigLoader {
            this()?.inputStream()?.use {
                Properties().apply {
                    load(it)
                }
            }
        }

        fun ConfigLoader<ByteArray>.string(charset: Charset = Charsets.UTF_8) = ConfigLoader {
            this()?.toString(charset)
        }
    }

    infix fun or(fallback: ConfigLoader<T>) = ConfigLoader { this() ?: fallback() }
    fun get() = this()
    fun orThrow(): T = this() ?: throw IllegalArgumentException("Could not find resource")
}