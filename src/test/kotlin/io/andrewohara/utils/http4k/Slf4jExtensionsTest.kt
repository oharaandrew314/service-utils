package io.andrewohara.utils.http4k

import io.andrewohara.utils.jdk.toClock
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import org.http4k.filter.ResponseFilters
import org.http4k.filter.ServerFilters
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveHeader
import org.http4k.kotest.shouldNotHaveHeader
import org.junit.jupiter.api.Test
import org.slf4j.helpers.BasicMDCAdapter
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant

class Slf4jExtensionsTest {

    companion object {
        private const val requestIdName = "rid"
    }

    private val logger = InMemoryLogger()
    private val clock = Instant.ofEpochSecond(1337).toClock()
    private val mdc = BasicMDCAdapter()

    private val server: HttpHandler = { request ->
        clock += Duration.ofMillis(10)
        Response(Status.OK).body(request.header(requestIdName) ?: "")
    }

    private val request = Request(Method.GET, "/")

    @Test
    fun `log response status - should log`() {
        ResponseFilters.logSummary(logger, clock) { _, _ -> true }
            .then(server)(request)

        logger.shouldContainExactly("GET /: 200 OK in 10 ms")
    }

    @Test
    fun `log response status - should not log`() {
        ResponseFilters.logSummary(logger, clock) { _, _ -> false }
            .then(server)(request)

        logger.shouldBeEmpty()
    }

    @Test
    fun `requestId to MDC`() {
        val request = request.header(requestIdName, "lolcats")
        ServerFilters.requestIdToMdc(requestIdName, mdc)
            .then(server)(request)
            .shouldHaveHeader(requestIdName, "lolcats")

        mdc.get(requestIdName) shouldBe "lolcats"
    }

    @Test
    fun `requestId to MDC - no requestId`() {
        ServerFilters.requestIdToMdc(requestIdName, mdc)
            .then(server)(request)
            .shouldNotHaveHeader(requestIdName)

        mdc.get(requestIdName).shouldBeNull()
    }

    @Test
    fun `requestId to MDC - no requestId - log4j`() {
        val mdc = org.slf4j.log4j12.Log4jMDCAdapter()
        ServerFilters.requestIdToMdc(requestIdName, mdc).then(server)(request)

        mdc.get(requestIdName).shouldBeNull()
    }

    @Test
    fun `requestId to MDC - no requestId, with generator`() {
        ServerFilters.requestIdToMdc(requestIdName, mdc) { "trolls" }
            .then(server)(request)
            .shouldHaveHeader(requestIdName, "trolls")

        mdc.get(requestIdName) shouldBe "trolls"
    }

    @Test
    fun `MDC to requestId`() {
        mdc.put(requestIdName, "lolcats")
        
        ClientFilters.mdcToRequestId(requestIdName, mdc).then(server)(request) shouldHaveBody "lolcats"
    }

    @Test
    fun `MDC to requestId - with no requestId`() {
        ClientFilters.mdcToRequestId(requestIdName, mdc).then(server)(request) shouldHaveBody ""
    }

    @Test
    fun `MDC to requestId - with no requestId, but has generator`() {
        ClientFilters.mdcToRequestId(requestIdName, mdc) { "troll" }.then(server)(request) shouldHaveBody "troll"
    }

    @Test
    fun `log errors`() {
        shouldThrow<IllegalArgumentException> {
            ServerFilters.logErrors(logger).then { throw IllegalArgumentException("stuff") }(request)
        }
        logger.shouldContainExactly("Error during $request")
    }

    @Test
    fun `log errors - no error`() {
        ServerFilters.logErrors(logger).then(server)(request)
        logger.shouldBeEmpty()
    }
}