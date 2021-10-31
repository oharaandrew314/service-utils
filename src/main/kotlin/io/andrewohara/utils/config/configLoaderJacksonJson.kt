package io.andrewohara.utils.config

import org.http4k.format.Jackson

inline fun <reified T> ConfigLoader<ByteArray>.jacksonJson() = ConfigLoader<T> { name ->
    this(name)?.inputStream()?.use {
        Jackson.asA(it)
    }
}