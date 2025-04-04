package dev.andrewohara.utils.ksuid

import com.github.ksuid.Ksuid
import com.github.ksuid.KsuidGenerator
import dev.andrewohara.utils.jdk.toClock
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Random

class MyKsuidValue private constructor(value: Ksuid): KsuidValue(value) {
    companion object: KsuidValueFactory<MyKsuidValue>(::MyKsuidValue)
}

class KsuidValues4kTest {

    @Test
    fun `generate id - override clock`() {
        val time = Instant.parse("2023-09-18T12:00:00Z")
        val random = Random(1337)

        val ksuid = MyKsuidValue.random(time.toClock(), KsuidGenerator(random))
        ksuid.value.toString() shouldBe "2VZKwSmOFrH28QKVG3qE3XVoMUp"
        ksuid.instant shouldBe time
    }

    @Test
    fun `generate id - override time`() {
        val time = Instant.parse("2023-09-18T12:00:00Z")
        val random = Random(1337)

        val ksuid = MyKsuidValue.random(time, KsuidGenerator(random))
        ksuid.value.toString() shouldBe "2VZKwSmOFrH28QKVG3qE3XVoMUp"
        ksuid.instant shouldBe time
    }

    @Test
    fun `parse id`() {
        val ksuid = MyKsuidValue.parse("2VaB6tloDoOktDfrzWsjLnnTe9T")
        ksuid.instant shouldBe Instant.parse("2023-09-18T19:08:58Z")
        ksuid.value.toString() shouldBe "2VaB6tloDoOktDfrzWsjLnnTe9T"
    }
}