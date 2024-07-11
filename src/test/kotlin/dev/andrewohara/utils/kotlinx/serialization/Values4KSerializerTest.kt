package dev.andrewohara.utils.kotlinx.serialization

import dev.forkhandles.values.AbstractValue
import dev.forkhandles.values.BooleanValueFactory
import dev.forkhandles.values.IntValue
import dev.forkhandles.values.IntValueFactory
import dev.forkhandles.values.LongValue
import dev.forkhandles.values.LongValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.StringValueFactory
import dev.forkhandles.values.UUIDValue
import dev.forkhandles.values.UUIDValueFactory
import io.andrewohara.utils.kotlinx.serialization.values4KSerializer
import io.andrewohara.utils.kotlinx.serialization.values4kSerializer
import io.kotest.matchers.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import org.junit.jupiter.api.Test
import java.util.UUID

class Values4KSerializerTest {

    @Serializable(with = TestStringValueSerializer::class)
    private class TestStringValue private constructor(value: String): StringValue(value) {
        companion object: StringValueFactory<TestStringValue>(::TestStringValue)
    }
    private object TestStringValueSerializer: KSerializer<TestStringValue> by values4kSerializer(TestStringValue)

    @Test
    fun `encode string value`() {
        val value = TestStringValue.of("foo")
        Json.encodeToString(value) shouldBe "\"foo\""
    }

    @Test
    fun `decode string value`() {
        val value = TestStringValue.of("foo")
        Json.decodeFromString<TestStringValue>("\"foo\"") shouldBe value
    }

    @Serializable(with = TestUuidValueSerializer::class)
    private class TestUuidValue private constructor(value: UUID): UUIDValue(value) {
        companion object: UUIDValueFactory<TestUuidValue>(::TestUuidValue)
    }
    private object TestUuidValueSerializer: KSerializer<TestUuidValue> by values4kSerializer(TestUuidValue)

    @Test
    fun `encode uuid value`() {
        val value = TestUuidValue.of(UUID.fromString("5f3cf708-fd01-4ccf-9009-abc01b5701b1"))
        Json.encodeToString(value) shouldBe "\"5f3cf708-fd01-4ccf-9009-abc01b5701b1\""
    }

    @Test
    fun `decode uuid value`() {
        val value = TestUuidValue.of(UUID.fromString("5f3cf708-fd01-4ccf-9009-abc01b5701b1"))
        Json.decodeFromString<TestUuidValue>("\"5f3cf708-fd01-4ccf-9009-abc01b5701b1\"") shouldBe value
    }

    @Serializable(with = TestIntValueSerializer::class)
    private class TestIntValue private constructor(value: Int): IntValue(value) {
        companion object: IntValueFactory<TestIntValue>(::TestIntValue)
    }
    private object TestIntValueSerializer: KSerializer<TestIntValue> by values4kSerializer(TestIntValue)

    @Test
    fun `encode int value`() {
        val value = TestIntValue.of(1)
        Json.encodeToJsonElement(value) shouldBe JsonPrimitive(1)
    }

    @Test
    fun `decode int value`() {
        val value = TestIntValue.of(1)
        Json.decodeFromJsonElement<TestIntValue>(JsonPrimitive(1)) shouldBe value
    }

    @Serializable(with = TestLongValueSerializer::class)
    private class TestLongValue private constructor(value: Long): LongValue(value) {
        companion object: LongValueFactory<TestLongValue>(::TestLongValue)
    }
    private object TestLongValueSerializer: KSerializer<TestLongValue> by values4KSerializer(TestLongValue)

    @Test
    fun `encode long value`() {
        val value = TestLongValue.of(1337L)
        Json.encodeToJsonElement(value) shouldBe JsonPrimitive(1337L)
    }

    @Test
    fun `decode long value`() {
        val value = TestLongValue.of(1337L)
        Json.decodeFromJsonElement<TestLongValue>(JsonPrimitive(1337L)) shouldBe value
    }

    @Serializable(with = TestBooleanValueSerializer::class)
    private class TestBooleanValue private constructor(value: Boolean): AbstractValue<Boolean>(value) {
        companion object: BooleanValueFactory<TestBooleanValue>(::TestBooleanValue)
    }
    private object TestBooleanValueSerializer: KSerializer<TestBooleanValue> by values4kSerializer(TestBooleanValue)

    @Test
    fun `encode boolean value`() {
        val value = TestBooleanValue.of(true)
        Json.encodeToJsonElement(value) shouldBe JsonPrimitive(true)
    }

    @Test
    fun `decode boolean value`() {
        val value = TestBooleanValue.of(true)
        Json.decodeFromJsonElement<TestBooleanValue>(JsonPrimitive(true)) shouldBe value
    }
}