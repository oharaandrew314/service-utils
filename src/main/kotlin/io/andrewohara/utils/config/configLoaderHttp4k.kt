package io.andrewohara.utils.config

import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.format.AutoMarshalling
import java.io.IOException

fun ConfigLoader.Companion.http4k(backend: HttpHandler) = ConfigLoader { name ->
    val response = backend(Request(Method.GET, name))
    when(response.status) {
        Status.OK -> response.body.stream.readBytes()
        Status.NOT_FOUND -> null
        else -> throw IOException(response.toMessage())
    }
}

fun ConfigLoader.Companion.http4k(
    baseUri: String,
    backend: HttpHandler
) = http4k(Uri.of(baseUri), backend)

fun ConfigLoader.Companion.http4k(
    baseUri: Uri,
    backend: HttpHandler
) = http4k(ClientFilters.SetBaseUriFrom(baseUri).then(backend))

inline fun <reified T> ConfigLoader<ByteArray>.mapped(marshaller: AutoMarshalling) = ConfigLoader<T> { name ->
    this(name)?.inputStream()?.use { stream ->
        marshaller.asA(stream)
    }
}