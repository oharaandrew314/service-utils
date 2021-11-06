package io.andrewohara.utils.jdbc

import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant

fun PreparedStatement.setNullableString(position: Int, value: String?, jdbcType: Int = Types.VARCHAR) {
    if (value != null) {
        setString(position, value)
    } else {
        setNull(position, jdbcType)
    }
}

fun PreparedStatement.setNullableInt(position: Int, value: Int?, jdbcType: Int = Types.INTEGER) {
    if (value != null) {
        setInt(position, value)
    } else {
        setNull(position, jdbcType)
    }
}

fun PreparedStatement.setNullableInstant(position: Int, timestamp: Instant?, jdbcType: Int = Types.TIMESTAMP) {
    if (timestamp != null) {
        setTimestamp(position, Timestamp.from(timestamp))
    } else {
        setNull(position, jdbcType)
    }
}

fun PreparedStatement.setNullableBoolean(position: Int, value: Boolean?, jdbcType: Int = Types.BOOLEAN) {
    if (value != null) {
        setBoolean(position, value)
    } else {
        setNull(position, jdbcType)
    }
}