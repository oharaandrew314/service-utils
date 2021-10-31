package io.andrewohara.utils.jdbc

import com.mysql.cj.MysqlType
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant
import java.util.*

fun PreparedStatement.setNullableString(position: Int, value: String?, mysqlType: MysqlType = MysqlType.VARCHAR) {
    if (value != null) {
        setString(position, value)
    } else {
        setNull(position, mysqlType.jdbcType)
    }
}

fun PreparedStatement.setNullableInt(position: Int, value: Int?, mysqlType: MysqlType = MysqlType.INT) {
    if (value != null) {
        setInt(position, value)
    } else {
        setNull(position, mysqlType.jdbcType)
    }
}

fun PreparedStatement.setNullableInstant(position: Int, timestamp: Instant?) {
    if (timestamp != null) {
        setTimestamp(position, Timestamp.from(timestamp))
    } else {
        setNull(position, MysqlType.TIMESTAMP.jdbcType)
    }
}

fun ResultSet.getLocale(key: String): Locale? = getString(key)?.let { Locale.forLanguageTag(it) }

fun ResultSet.getNullableInteger(key: String): Int? = getBigDecimal(key)?.toInt()

fun ResultSet.getNullableBoolean(key: String): Boolean? = getBoolean(key).let { if (wasNull()) null else it }