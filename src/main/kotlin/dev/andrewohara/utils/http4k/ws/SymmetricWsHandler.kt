package dev.andrewohara.utils.http4k.ws

import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.testing.TestWebsocket
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsHandler

typealias SymmetricWsHandler = (Request) -> Websocket

fun interface SymmetricWsFilter : (SymmetricWsHandler) -> SymmetricWsHandler {
    companion object
}

fun SymmetricWsFilter.then(next: SymmetricWsFilter): SymmetricWsFilter = SymmetricWsFilter { this(next(it)) }
fun SymmetricWsFilter.then(next: SymmetricWsHandler): SymmetricWsHandler = this(next)

val SymmetricWsFilter.Companion.NoOp: SymmetricWsFilter get() = SymmetricWsFilter { it }
fun SymmetricWsFilter.Companion.SetHostFrom(uri: Uri): SymmetricWsFilter = SymmetricWsFilter { next ->
    {
        next(it.uri(it.uri.scheme(uri.scheme).host(uri.host).port(uri.port))
            .replaceHeader("Host", "${uri.host}${uri.port?.let { port -> ":$port" } ?: ""}"))
    }
}

fun WsHandler.toSymmetric(): SymmetricWsHandler = { TestWebsocket(invoke(it)) }