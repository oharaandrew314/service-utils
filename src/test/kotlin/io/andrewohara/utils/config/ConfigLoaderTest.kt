package io.andrewohara.utils.config

import io.andrewohara.utils.config.ConfigLoader.Companion.properties
import io.andrewohara.utils.config.ConfigLoader.Companion.string
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.*

data class Config(
    val string: String = "foo",
    val nullable: String? = null,
    val int: Int = 1337,
    val enum: Option = Option.A,
    val instant: Instant = Instant.parse("2021-01-01T01:00:00Z"),
    val nested: Nested = Nested(string = "bar")
) {
    enum class Option { A }
    data class Nested(val string: String)
}


class ConfigLoaderTest {

    @Test
    fun `string from file`() {
        val file = Files.createTempFile("foo", "txt")
        try {
            Files.writeString(file, "foo bar baz")

            ConfigLoader.file(file).string()() shouldBe "foo bar baz"
        } finally {
            file.toFile().delete()
        }
    }

    @Test
    fun `missing file should be null`() {
        ConfigLoader.file(Path.of("missing.txt"))().shouldBeNull()
    }

    @Test
    fun `jackson json`() {
        ConfigLoader.resource("config.json").jacksonJson<Config>()() shouldBe Config()
    }

    @Test
    fun `moshi json`() {
        ConfigLoader.resource("config.json").moshiJson<Config>()() shouldBe Config()
    }

    @Test
    fun `gson json`() {
        ConfigLoader.resource("config.json").gsonJson<Config>()() shouldBe Config()
    }

    @Test
    fun `jackson yaml`() {
        ConfigLoader.resource("config.json").jacksonYaml<Config>()() shouldBe Config()
    }

    @Test
    fun `missing resource should be null`() {
        ConfigLoader.resource("missing.json")().shouldBeNull()
    }

    @Test
    fun `missing resource should throw`() {
        shouldThrow<IllegalArgumentException> {
            ConfigLoader.resource("missing.json").orThrow()
        }
    }

    @Test
    fun `properties from resource`() {
        ConfigLoader.resource("config.properties").properties()() shouldBe Properties().apply {
            put("foo", "bar")
            put("toll", "troll")
        }
    }

    @Test
    fun http() {
        val server = routes(
            "/stuff.txt" bind Method.GET to { Response(Status.OK).body("foo bar baz") }
        )

        ConfigLoader.http4k(Uri.of("http://localhost/stuff.txt"), server).string()() shouldBe "foo bar baz"
    }

    @Test
    fun `missing from http`() {
        val server = routes(
            "/stuff.txt" bind Method.GET to { Response(Status.OK).body("foo bar baz") }
        )

        ConfigLoader.http4k(Uri.of("http://localhost/missing.txt"), server).string()().shouldBeNull()
    }

    @Test
    fun `fallback to next in chain`() {
        (ConfigLoader.resource("missing.txt") or ConfigLoader.resource("text2.txt")).string()() shouldBe "toll trolls"
    }

    @Test
    fun `chain hits on first try`() {
        (ConfigLoader.resource("text.txt") or ConfigLoader.resource("text2.txt")).string()() shouldBe "foo bar baz"
    }

    @Test
    fun `missing from chain should be null`() {
        (ConfigLoader.resource("missing.txt") or ConfigLoader.resource("missing2.txt")).string()().shouldBeNull()
    }

    @Test
    fun `missing from chain should throw`() {
        shouldThrow<IllegalArgumentException> {
            (ConfigLoader.resource("missing.txt") or ConfigLoader.resource("missing2.txt")).string().orThrow()
        }
    }
}