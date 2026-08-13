package dev.andrewohara.utils.values4k

import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory
import org.http4k.lens.BiDiMapping

inline fun <reified DOMAIN: Value<PRIMITIVE>, PRIMITIVE: Any> ValueFactory<DOMAIN, PRIMITIVE>.toBiDiMapping() =
    BiDiMapping<String, DOMAIN>( ::parse, ::show)