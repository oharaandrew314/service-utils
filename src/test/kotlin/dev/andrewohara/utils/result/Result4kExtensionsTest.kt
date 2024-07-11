package dev.andrewohara.utils.result

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.kotest.shouldBeFailure
import dev.forkhandles.result4k.kotest.shouldBeSuccess
import org.junit.jupiter.api.Test

class RecoverIfTests {

    @Test
    fun `never re-recover a success`() {
        Success("a")
            .recoverIf({ true }, { "b" })
            .shouldBeSuccess("a")
    }

    @Test
    fun `recover a failure when condition is met`() {
        Failure(1)
            .recoverIf({ it == 1 }, { "a" })
            .shouldBeSuccess("a")
    }

    @Test
    fun `don't recover a failure when condition is not met`() {
        Failure(1)
            .recoverIf({ it == 2 }, { "a" })
            .shouldBeFailure(1)
    }
}

class FailIfTests {

    @Test
    fun `never re-fail a failure`() {
        Failure(1)
            .failIf({ true }, { 2 })
            .shouldBeFailure(1)
    }

    @Test
    fun `fail a success when condition is met`() {
        Success("a")
            .failIf({ it == "a" }, { 1 })
            .shouldBeFailure(1)
    }

    @Test
    fun `don't fail a success when condition is not met`() {
        Success("a")
            .failIf({ it == "b" }, { 1 })
            .shouldBeSuccess("a")
    }
}