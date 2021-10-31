package io.andrewohara.utils.config

import org.http4k.format.Moshi

inline fun <reified T> ConfigLoader<ByteArray>.moshiJson() = ConfigLoader<T> { name ->
    this(name)?.inputStream()?.use {
        Moshi.asA(it)
    }
}