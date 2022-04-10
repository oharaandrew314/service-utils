package io.andrewohara.utils.http4k

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.filter.ResponseFilters
import java.net.http.HttpClient
import java.time.Duration

fun javaHttpClient(
    timeout: Duration? = null,
    hostUri: Uri? = null,
    logSummary: Boolean = false,
    requestBodyMode: BodyMode = BodyMode.Memory,
    responseBodyMode: BodyMode = BodyMode.Memory,
): HttpHandler {
    val client = HttpClient.newBuilder().apply {
        version(HttpClient.Version.HTTP_1_1)
        followRedirects(HttpClient.Redirect.NEVER)
        if (timeout != null) connectTimeout(timeout)
    }.build()

    val backend = JavaHttpClient(
        httpClient = client,
        requestBodyMode = requestBodyMode,
        responseBodyMode = responseBodyMode
    )

    return Filter.NoOp
        .let { if (hostUri == null) it else ClientFilters.SetHostFrom(hostUri) }
        .let { if (logSummary) ResponseFilters.logSummary() else it }
        .then(backend)
}