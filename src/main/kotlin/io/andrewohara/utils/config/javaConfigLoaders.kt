package io.andrewohara.utils.config

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun ConfigLoader.Companion.java(baseUrl: String): ConfigLoader<ByteArray> {
    val client = HttpClient.newHttpClient()
    return ConfigLoader { name ->
        val request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("${baseUrl.trimEnd('/')}/${name.trimStart('/')}"))
            .build()

        client.send(request, HttpResponse.BodyHandlers.ofByteArray())
            .takeIf { it.statusCode() == 200 }
            ?.body()
    }
}