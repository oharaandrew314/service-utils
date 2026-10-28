package dev.andrewohara.utils.kotlinx.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Duration
import java.time.Instant
import java.util.UUID

object Iso8601InstantSerializer: KSerializer<Instant> by object: KSerializer<Instant> {
    override val descriptor = PrimitiveSerialDescriptor("instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder) = Instant.parse(decoder.decodeString())
}

object Iso8601DurationSerializer: KSerializer<Duration> by object: KSerializer<Duration> {
    override val descriptor = PrimitiveSerialDescriptor("duration", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = Duration.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Duration) = encoder.encodeString(value.toString())
}

object UuidSerializer: KSerializer<UUID> by object: KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("uuid", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder) = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}