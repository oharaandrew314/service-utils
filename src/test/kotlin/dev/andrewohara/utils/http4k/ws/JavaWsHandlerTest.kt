package dev.andrewohara.utils.http4k.ws

import io.kotest.matchers.collections.shouldHaveSize
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri
import org.http4k.server.JettyLoom
import org.http4k.server.asServer
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class JavaWsHandlerTest {

    private val messages = mutableListOf<String>()
    private val wsHandler = { _: Request ->
        WsResponse { ws ->
            ws.onMessage {
                messages += it.bodyString()
                waitForMessage.countDown()
            }
        }
    }
    private val waitForMessage = CountDownLatch(1)

    private val server = wsHandler.asServer(JettyLoom(0)).start()

    private val client = SymmetricWsFilter.SetHostFrom(Uri.of("ws://localhost:${server.port()}"))
        .then(JavaWsHandler(timeout = Duration.ofSeconds(1)))

    @Test
    fun `open websocket through client`() {
        client(Request(Method.GET, "/"))
            .send(WsMessage("hi"))
        waitForMessage.await(1, TimeUnit.SECONDS)

        messages.shouldHaveSize(1)
    }
}