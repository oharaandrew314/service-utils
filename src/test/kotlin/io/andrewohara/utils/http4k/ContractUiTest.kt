package io.andrewohara.utils.http4k

import org.http4k.contract.contract
import org.http4k.contract.div
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.FOUND
import org.http4k.kotest.shouldHaveBody
import org.http4k.kotest.shouldHaveHeader
import org.http4k.kotest.shouldHaveStatus
import org.http4k.lens.Path
import org.http4k.lens.int
import org.junit.jupiter.api.Test

class ContractUiTest {

    private val userId = Path.int().of("user_id")

    private val getUser = "users" / userId bindContract GET to { userId ->
        {
            Response(OK).body("user$userId")
        }
    }

    private val contract = ContractUi(
        contract = contract {
            renderer = OpenApi3(ApiInfo(title = "API", version = "v1.0"))
            descriptionPath = "openapi3.json"
            routes += getUser
        },
        descriptionPath = "openapi3.json",
        pageTitle = "Test API"
    )

    @Test
    fun `contract route 200`() {
        val response = contract(Request(GET, "/users/123"))
        response shouldHaveStatus OK
        response shouldHaveBody "user123"
    }

    @Test
    fun `contract route 404`() {
        val response = contract(Request(GET, "/people/123"))
        response shouldHaveStatus NOT_FOUND
    }

    @Test
    fun `get default response`() {
        val response = contract(Request(GET, "/"))

        response shouldHaveStatus FOUND
        response.shouldHaveHeader("Location", "swagger")
    }

    @Test
    fun `get swagger ui`() {
        val response = contract(Request(GET, "/swagger"))

        response shouldHaveStatus OK
//        response shouldHaveBody javaClass.classLoader.getResourceAsStream("swagger-ui.html")!!.reader().readText()
    }

    @Test
    fun `get openapi spec`() {
        val response = contract(Request(GET, "/openapi3.json"))

        val expected = javaClass.classLoader.getResourceAsStream("api.json")!!.reader().readText()

        response shouldHaveStatus OK
        response shouldHaveBody expected
    }

//    @Test
//    fun run() {
//        contract.asServer(SunHttp(8000)).start().block()
//    }

//    @Test FIXME
//    fun `multiple specs`() {
//        val server = routes(
//            "v1" bind contract,
//            "v2" bind contract
//        )
//
//        server(Request(GET, "/v1/users/123")) shouldHaveStatus OK
//        server(Request(GET, "/v1")).shouldHaveHeader("Location", "index.html?url=openapi3.json")
//        server(Request(GET, "/v1/index.html?url=openapi3.json")) shouldHaveStatus OK
//
//        server(Request(GET, "/v2/users/456")) shouldHaveStatus OK
//        server(Request(GET, "/v2")).shouldHaveHeader("Location", "index.html?url=openapi3.json")
//        server(Request(GET, "/v2/index.html?url=openapi3.json")) shouldHaveStatus OK
//    }
}

