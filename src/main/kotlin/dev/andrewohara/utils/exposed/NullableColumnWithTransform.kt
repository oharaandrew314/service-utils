package dev.andrewohara.utils.exposed

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.sql.Column
import kotlin.reflect.KProperty

open class NullableColumnWithTransform<TColumn, TReal, ID : Comparable<ID>>(
    private val column: Column<TColumn?>,
    private val toColumn: (TReal) -> TColumn,
    private val toReal: (TColumn) -> TReal
) {
    companion object {
        operator fun <TColumn, TReal, ID : Comparable<ID>> invoke(
            entityClass: EntityClass<ID, *>,
            column: Column<TColumn?>,
            toColumn: (TReal) -> TColumn,
            toReal: (TColumn) -> TReal
        ) = NullableColumnWithTransform<TColumn, TReal, ID>(column, toColumn, toReal)
    }

    operator fun getValue(o: Entity<ID>, desc: KProperty<*>): TReal? {
        return with(o) {
            column.getValue(o, desc)?.let(toReal)
        }
    }

    operator fun setValue(o: Entity<ID>, desc: KProperty<*>, value: TReal?) {
        with(o) {
            column.setValue(o, desc, value?.let(toColumn))
        }
    }
}
