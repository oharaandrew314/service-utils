package io.andrewohara.utils.config

import org.http4k.format.Moshi

inline fun <reified T> ConfigLoader<ByteArray>.moshiJson() = ConfigLoader<T> {
    this()?.inputStream()?.use {
        Moshi.asA(it)
    }
}