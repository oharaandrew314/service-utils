package io.andrewohara.utils.jdbc4k

import io.andrewohara.utils.jdbc.*
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.time.Instant
import javax.sql.DataSource

fun DataSource.forK() = DataSource4k(this)

class DataSource4k(val java: DataSource) {

    fun <T> connection(fn: (Connection4K) -> T): T = java.connection.let(::Connection4K).use(fn)
}

private val placeholders = ":([0-9a-zA-Z\$_]+)".toRegex()
private fun String.processStatement(): Pair<String, Map<String, List<Int>>> {
    val matches = mutableListOf<String>()
    val updatedSql = placeholders.replace(this) {
        matches += it.groupValues[1]
        "?"
    }

    val mappings = matches.withIndex()
        .groupBy { it.value }
        .mapValues { (_, value) -> value.map { it.index + 1 } }

    return updatedSql to mappings
}

class Connection4K(val java: Connection): AutoCloseable by java {
    fun <T> preparedStatement(
        sql: String,
        fn: (PreparedStatement4k) -> T
    ): T {
        val (updatedSql, mappings) = sql.processStatement()
        val preparedStatement = java.prepareStatement(updatedSql)
        return PreparedStatement4k(preparedStatement, mappings).use(fn)
    }

    fun <T> preparedStatementWithKeys(
        sql: String,
        fn: (PreparedStatement4kWithKeys) -> T
    ): T {
        val (updatedSql, mappings) = sql.processStatement()

        val preparedStatement = java.prepareStatement(updatedSql, Statement.RETURN_GENERATED_KEYS)
        return PreparedStatement4kWithKeys(preparedStatement, mappings).use(fn)
    }

    fun <T> statement(fn: (Statement4k) -> T): T {
        val statement = java.createStatement()
        return Statement4k(statement).use(fn)
    }

    fun <T> statementWithKeys(fn: (Statement4kWithKeys) -> T): T {
        val statement = java.createStatement(Statement.RETURN_GENERATED_KEYS, ResultSet.CONCUR_READ_ONLY)
        return Statement4kWithKeys(statement).use(fn)
    }
}

abstract class PreparedStatementInjector(
    val java: PreparedStatement,
    private val mappings: Map<String, List<Int>>
): AutoCloseable by java {
    private fun indices(name: String, fn: (Int) -> Unit) {
        mappings[name]
            ?.forEach(fn)
            ?: throw IllegalArgumentException("Parameter $name not found")
    }

    operator fun set(name: String, value: String?) = indices(name) { java.setNullableString(it, value) }
    operator fun set(name: String, value: Int?) = indices(name) { java.setNullableInt(it, value) }
}

private fun Statement.keySequence() = sequence {
    generatedKeys.use { rs ->
        while(rs.next()) {
            yield(ResultSet4K(rs))
        }
    }
}

class Statement4k(val java: Statement): AutoCloseable by java {

    fun execute(sql: String): Boolean = java.execute(sql)

    fun executeUpdate(sql: String): Int = java.executeUpdate(sql)

    fun executeQuery(sql: String): Sequence<ResultSet4K> {
        return java.executeQuery(sql).toSequence().map(::ResultSet4K)
    }

    fun batch(vararg sql: String): IntArray {
        for (line in sql) {
            java.addBatch(line)
        }
        return java.executeBatch()
    }
}

class Statement4kWithKeys(val java: Statement): AutoCloseable by java {

    fun <T> executeUpdate(sql: String, onGeneratedKeys: ((Sequence<ResultSet4K>) -> T)): T {
        java.executeUpdate(sql)
        return java.keySequence().let(onGeneratedKeys)
    }

    fun <Result> batch(vararg sql: String, keysFn: (Sequence<ResultSet4K>) -> Result): Result {
        for (line in sql) {
            java.addBatch(line)
        }
        java.executeBatch()

        return java.keySequence().let(keysFn)
    }
}

class PreparedStatement4k(
    java: PreparedStatement,
    mappings: Map<String, List<Int>>
): PreparedStatementInjector(java, mappings) {
    fun execute(): Boolean = java.execute()
    fun executeUpdate(): Int = java.executeUpdate()
    fun executeQuery(): Sequence<ResultSet4K> {
        return java.executeQuery().toSequence().map(::ResultSet4K)
    }

    fun <Item: Any> batch(vararg items: Item, itemFn: Item.(PreparedStatementInjector) -> Unit) = batch(items.toList(), itemFn)

    fun <Item: Any> batch(items: Collection<Item>, itemFn: Item.(PreparedStatementInjector) -> Unit): IntArray {
        for (item in items) {
            itemFn(item, this)
            java.addBatch()
        }
        return java.executeBatch()
    }
}

class PreparedStatement4kWithKeys(
    java: PreparedStatement,
    mappings: Map<String, List<Int>>
): PreparedStatementInjector(java, mappings) {
    fun <T> executeUpdate(onGeneratedKey: (ResultSet4K) -> T): T? {
        java.executeUpdate()
        return java.keySequence().firstOrNull()?.let(onGeneratedKey)
    }

    fun <Item: Any, Result> batch(items: Collection<Item>, itemFn: Item.(PreparedStatementInjector) -> Unit, keyFn: (ResultSet4K) -> Result): List<Result> {
        for (item in items) {
            itemFn(item, this)
            java.addBatch()
        }
        java.executeBatch()

        return java.keySequence().map(keyFn).toList()
    }
}

class ResultSet4K(val java: ResultSet) {
    fun getString(name: String): String? = java.getStringOrNull(name)
    fun getBoolean(name: String): Boolean? = java.getBoolOrNull(name)
    fun getInt(name: String): Int? = java.getIntOrNull(name)
    fun getLong(name: String): Long? = java.getBigDecimal(name)?.toLong()
    fun getFloat(name: String): Float? = java.getBigDecimal(name)?.toFloat()
    fun getDouble(name: String): Double? = java.getBigDecimal(name)?.toDouble()
    fun getBigInteger(name: String): BigInteger? = java.getBigDecimal(name)?.toBigInteger()
    fun getBigDecimal(name: String): BigDecimal? = java.getBigDecimal(name)
    fun getByteArray(name: String): ByteArray? = java.getBytes(name)
    fun getInputStream(name: String): InputStream? = java.getBinaryStream(name)
    fun getReader(name: String): Reader? = java.getCharacterStream(name)
    fun getInstant(name: String): Instant? = java.getTimestamp(name)?.toInstant()
    inline fun <reified T: Enum<T>> ResultSet.getEnum(name: String): T? {
        return getString(name)?.let {
            enumValueOf<T>(it)
        }
    }
}