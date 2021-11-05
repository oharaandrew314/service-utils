package io.andrewohara.utils.config

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import java.io.IOException

fun ConfigLoader.Companion.http4k(backend: HttpHandler = JavaHttpClient()) = ConfigLoader { name ->
    val response = backend(Request(Method.GET, name))
    when(response.status) {
        Status.OK -> response.body.stream.readAllBytes()
        Status.NOT_FOUND -> null
        else -> throw IOException(response.toMessage())
    }
}

fun ConfigLoader.Companion.http4k(
    baseUri: Uri,
    backend: HttpHandler = JavaHttpClient(),
) = http4k(ClientFilters.SetBaseUriFrom(baseUri).then(backend))