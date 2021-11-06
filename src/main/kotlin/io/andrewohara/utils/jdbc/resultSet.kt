package io.andrewohara.utils.jdbc

import java.sql.ResultSet
import java.time.Instant

fun ResultSet.getNullableInteger(key: String): Int? = getBigDecimal(key)?.toInt()

fun ResultSet.getNullableBoolean(key: String): Boolean? = getBoolean(key).let { if (wasNull()) null else it }

fun ResultSet.getNullableInstant(key: String): Instant? = getTimestamp(key)?.toInstant()