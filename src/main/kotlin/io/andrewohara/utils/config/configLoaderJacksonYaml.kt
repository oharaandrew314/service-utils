package io.andrewohara.utils.config

import org.http4k.format.JacksonYaml

inline fun <reified T> ConfigLoader<ByteArray>.jacksonYaml() = ConfigLoader<T> { name ->
    this(name)?.inputStream()?.use {
        JacksonYaml.asA(it)
    }
}