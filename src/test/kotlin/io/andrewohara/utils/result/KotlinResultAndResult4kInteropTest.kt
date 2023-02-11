package io.andrewohara.utils.result

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Success
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class KotlinResultAndResult4kInteropTest {

    @Test
    fun `Ok to Success`() {
        Ok(1).toResult4k() shouldBe Success(1)
    }

    @Test
    fun `Err to Failure`() {
        Err(1).toResult4k() shouldBe Failure(1)
    }

    @Test
    fun `Success to Ok`() {
        Success(1).toKotlinResult() shouldBe Ok(1)
    }

    @Test
    fun `Failure to Err`() {
        Failure(1).toKotlinResult() shouldBe Err(1)
    }
}