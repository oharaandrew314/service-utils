package dev.andrewohara.utils.kotlinx.serialization

import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigInteger

private fun <VALUE : Value<WRAPPED>, WRAPPED: Any, PRIM : Any> serializer(
    serialName: String,
    kind: PrimitiveKind,
    parseFn: (PRIM) -> VALUE,
    showFn: (VALUE) -> PRIM,
    decodeFn: Decoder.() -> PRIM,
    encodeFn: Encoder.(PRIM) -> Unit
) = object: KSerializer<VALUE> {
    override val descriptor = PrimitiveSerialDescriptor(serialName, kind)
    override fun deserialize(decoder: Decoder): VALUE = parseFn(decodeFn(decoder))
    override fun serialize(encoder: Encoder, value: VALUE) = encodeFn(encoder, showFn(value))
}

@JvmName("stringSerializer")
fun <VALUE : Value<T>, T: Any> values4kSerializer(fn: ValueFactory<VALUE, T>) = serializer(
    serialName = "StringSerializer",
    kind = PrimitiveKind.STRING,
    parseFn = { fn.parse(it) },
    showFn = { fn.show(it) },
    decodeFn = { decodeString() },
    encodeFn = { encodeString(it) }
)

@JvmName("intSerializer")
fun <VALUE : Value<Int>> values4kSerializer(fn: ValueFactory<VALUE, Int>) = serializer(
    serialName = "IntSerializer",
    kind = PrimitiveKind.INT,
    parseFn = { fn.of(it) },
    showFn = { fn.unwrap(it) },
    decodeFn = { decodeInt() },
    encodeFn = { encodeInt(it) }
)

@JvmName("longSerializer")
fun <VALUE : Value<Long>> values4KSerializer(fn: ValueFactory<VALUE, Long>) = serializer(
    serialName = "LongSerializer",
    kind = PrimitiveKind.LONG,
    parseFn = { fn.of(it) },
    showFn = { fn.unwrap(it) },
    decodeFn = { decodeLong() },
    encodeFn = { encodeLong(it) }
)

@JvmName("booleanSerializer")
fun <VALUE : Value<Boolean>> values4kSerializer(fn: ValueFactory<VALUE, Boolean>) = serializer(
    serialName = "BooleanSerializer",
    kind = PrimitiveKind.BOOLEAN,
    parseFn = { fn.of(it) },
    showFn = { fn.unwrap(it) },
    decodeFn = { decodeBoolean() },
    encodeFn = { encodeBoolean(it) }
)