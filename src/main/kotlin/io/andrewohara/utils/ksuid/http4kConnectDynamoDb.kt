package io.andrewohara.utils.ksuid

import com.github.ksuid.Ksuid
import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory
import org.http4k.connect.amazon.dynamodb.model.Attribute
import org.http4k.connect.amazon.dynamodb.model.value

// DynamoDb adapter for Ksuid values
fun <V: Value<Ksuid>> Attribute.Companion.value(vf: ValueFactory<V, Ksuid>): Attribute.AttrLensSpec<V> = string()
    .map(Ksuid::fromString, Ksuid::toString)
    .value(vf)