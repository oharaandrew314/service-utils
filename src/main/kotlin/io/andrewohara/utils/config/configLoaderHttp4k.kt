package io.andrewohara.utils.config

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import java.io.IOException

fun ConfigLoader.Companion.http4k(uri: Uri, backend: HttpHandler = JavaHttpClient()) = ConfigLoader {
    val response = backend(Request(Method.GET, uri))
    when(response.status) {
        Status.OK -> response.body.stream.readBytes()
        Status.NOT_FOUND -> null
        else -> throw IOException(response.toMessage())
    }
}