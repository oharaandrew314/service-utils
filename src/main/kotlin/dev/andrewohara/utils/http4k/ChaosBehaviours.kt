package dev.andrewohara.utils.http4k

import org.http4k.chaos.Behaviour
import org.http4k.chaos.ChaosBehaviours
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response

fun ChaosBehaviours.tapRequest(block: (Request) -> Unit) = object: Behaviour() {
    override fun invoke(next: HttpHandler) = { request: Request ->
        block(request)
        next(request)
    }
}

fun ChaosBehaviours.tapResponse(block: (Response) -> Unit) = object: Behaviour() {
    override fun invoke(next: HttpHandler) = { request: Request ->
        next(request).also(block)
    }
}