package dev.andrewohara.utils.kotlinx.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.http4k.core.Uri

object UriSerializer: KSerializer<Uri> {
    override val descriptor = PrimitiveSerialDescriptor("uri", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Uri) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder) = Uri.of(decoder.decodeString())
}