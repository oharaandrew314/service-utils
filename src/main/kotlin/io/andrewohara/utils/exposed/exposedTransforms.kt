package io.andrewohara.utils.exposed

import dev.forkhandles.values.Value
import dev.forkhandles.values.ValueFactory
import org.jetbrains.exposed.dao.ColumnWithTransform
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Column

fun <TColumn: Any, VALUE: Value<TColumn>> Column<TColumn>.value(
    fn: ValueFactory<VALUE, TColumn>
) = ColumnWithTransform(
    column = this,
    toReal = { fn.of(it) },
    toColumn = { fn.unwrap(it) }
)

fun <TColumn: Any, VALUE: Value<TColumn>, ID : Comparable<ID>> Column<TColumn?>.value(
    entityClass: EntityClass<ID, *>,
    fn: ValueFactory<VALUE, TColumn>
) = transform(
    entityClass = entityClass,
    toReal = { fn.of(it) },
    toColumn = { fn.unwrap(it) }
)

fun <TColumn: Any, TReal: Any, ID : Comparable<ID>> Column<TColumn?>.transform(
    entityClass: EntityClass<ID, *>,
    toColumn: (TReal) -> TColumn,
    toReal: (TColumn) -> TReal
) = NullableColumnWithTransform(
    entityClass = entityClass,
    column = this,
    toReal = toReal,
    toColumn = toColumn
)
