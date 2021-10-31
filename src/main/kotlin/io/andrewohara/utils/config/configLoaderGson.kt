package io.andrewohara.utils.config

import org.http4k.format.Gson

inline fun <reified T> ConfigLoader<ByteArray>.gsonJson() = ConfigLoader<T> { name ->
    this(name)?.inputStream()?.use {
        Gson.asA(it)
    }
}