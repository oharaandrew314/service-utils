package io.andrewohara.utils.config

import io.andrewohara.utils.mappers.gson
import io.andrewohara.utils.mappers.jacksonJson
import io.andrewohara.utils.mappers.jacksonYaml
import io.andrewohara.utils.mappers.moshi
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

data class Config(
    val string: String = "foo",
    val nullable: String? = null,
    val int: Int = 1337,
    val enum: Option = Option.A,
    val nested: Nested = Nested(string = "bar")
) {
    enum class Option { A }
    data class Nested(val string: String)
}


class ConfigLoaderTest {

    private val files = mutableListOf<Path>()
    private val server = routes(
        "/stuff.txt" bind Method.GET to { Response(Status.OK).body("foo bar baz") },
        "/error" bind Method.GET to { Response(Status.INTERNAL_SERVER_ERROR) }
    )

    private fun createFile(text: String): Path {
        return Files.createTempFile("foo", "txt").also { file ->
            files.add(file)
            Files.writeString(file, text)
        }
    }

    @AfterEach
    fun deleteFiles() {
        files.forEach { it.toFile().delete() }
    }

    @Test
    fun `string from file`() {
        val file = createFile("foo bar baz")

        ConfigLoader.file().string()(file.toString()) shouldBe "foo bar baz"
    }

    @Test
    fun `string from file with base directory`() {
        val file = createFile("lolcats")

        ConfigLoader.file(file.parent).string()(file.fileName.toString()) shouldBe "lolcats"
    }

    @Test
    fun `missing file should be null`() {
        ConfigLoader.file()("missing.txt").shouldBeNull()
    }

    @Test
    fun `jackson json`() {
        ConfigLoader.resource().jacksonJson<Config>()("config.json") shouldBe Config()
    }

    @Test
    fun `moshi json`() {
        ConfigLoader.resource().moshi<Config>()("config.json") shouldBe Config()
    }

    @Test
    fun `gson json`() {
        ConfigLoader.resource().gson<Config>()("config.json") shouldBe Config()
    }

    @Test
    fun `jackson yaml`() {
        ConfigLoader.resource().jacksonYaml<Config>()("config.json") shouldBe Config()
    }

    @Test
    fun `missing resource should be null`() {
        ConfigLoader.resource()("missing.json").shouldBeNull()
    }

    @Test
    fun `missing resource should throw`() {
        shouldThrow<IllegalArgumentException> {
            ConfigLoader.resource().orThrow("missing.json")
        }
    }

    @Test
    fun `properties from resource`() {
        ConfigLoader.resource().properties()("config.properties") shouldBe Properties().apply {
            put("foo", "bar")
            put("toll", "troll")
        }
    }

    @Test
    fun http() {
        val server = routes(
            "/stuff.txt" bind Method.GET to { Response(Status.OK).body("foo bar baz") }
        )

        ConfigLoader.http4k(server).string()("http://localhost/stuff.txt") shouldBe "foo bar baz"
    }

    @Test
    fun `http with base uri`() {
        ConfigLoader.http4k(Uri.of("http://localhost"), server).string()("stuff.txt") shouldBe "foo bar baz"
    }

    @Test
    fun `http error`() {
        shouldThrow<IOException> {
            ConfigLoader.http4k(server)("error")
        }
    }

    @Test
    fun `missing from http`() {
        ConfigLoader.http4k(server).string()("missing.txt").shouldBeNull()
    }

    @Test
    fun `fallback to next in chain`() {
        (ConfigLoader.file() or ConfigLoader.resource()).string()("text2.txt") shouldBe "toll trolls"
    }

    @Test
    fun `chain hits on first try`() {
        val file = createFile("lolcats")
        (ConfigLoader.file(file.parent) or ConfigLoader.resource()).string()(file.fileName.toString()) shouldBe "lolcats"
    }

    @Test
    fun `missing from chain should be null`() {
        (ConfigLoader.file() or ConfigLoader.resource()).string()("missing.txt").shouldBeNull()
    }

    @Test
    fun `missing from chain should throw`() {
        shouldThrow<IllegalArgumentException> {
            (ConfigLoader.file() or ConfigLoader.resource()).string().orThrow("missing.txt")
        }
    }

    @Test
    fun `missing string from env`() {
        ConfigLoader.env()("foo").shouldBeNull()
    }

    @Test
    fun `relative resource`() {
        ConfigLoader.resource(fromClassloader = false).string()("relative.txt") shouldBe "reality"
    }

    @Test
    fun `relative resource loader does not get root resource`() {
        ConfigLoader.resource(fromClassloader = false).string()("text.txt").shouldBeNull()
    }
}