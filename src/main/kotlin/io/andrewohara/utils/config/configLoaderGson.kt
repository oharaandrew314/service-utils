package io.andrewohara.utils.config

import org.http4k.format.Gson

inline fun <reified T> ConfigLoader<ByteArray>.gsonJson() = ConfigLoader<T> {
    this()?.inputStream()?.use {
        Gson.asA(it)
    }
}