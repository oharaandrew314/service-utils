package io.andrewohara.utils.jdbc

import java.sql.ResultSet
import java.time.Instant

fun ResultSet.getStringOrNull(key: String): String? = getString(key)

fun ResultSet.getIntOrNull(key: String): Int? = getBigDecimal(key)?.toInt()

fun ResultSet.getBoolOrNull(key: String): Boolean? = getBoolean(key).let { if (wasNull()) null else it }

fun ResultSet.getInstantOrNull(key: String): Instant? = getTimestamp(key)?.toInstant()

inline fun <reified T: Enum<T>> ResultSet.getEnumOrNull(name: String): T? {
    return getString(name)?.let {
        enumValueOf<T>(it)
    }
}

fun ResultSet.toSequence(): Sequence<ResultSet> = sequence {
    while(next()) {
        yield(this@toSequence)
    }
}