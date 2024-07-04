package io.andrewohara.utils.http4k.ws

import io.kotest.matchers.collections.shouldContainExactly
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.junit.jupiter.api.Test

class SymmetricWsHandlerTest {

    private val messages = mutableListOf<String>()
    private val handler = { _: Request ->
        WsResponse { ws ->
            ws.onMessage { messages += it.bodyString() }
        }
    }.toSymmetric()

    @Test
    fun `open websocket directly from handler`() {
        handler(Request(Method.GET, "/"))
            .send(WsMessage("hi"))

        messages.shouldContainExactly("hi")
    }
}