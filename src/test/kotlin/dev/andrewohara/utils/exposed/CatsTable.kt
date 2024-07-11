package dev.andrewohara.utils.exposed

import dev.forkhandles.values.NonEmptyStringValueFactory
import dev.forkhandles.values.StringValue
import dev.forkhandles.values.UUIDValue
import dev.forkhandles.values.UUIDValueFactory
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.IntEntityClass
import java.util.UUID


object CatsTable: IntIdTable("cats") {
    val name = text("name")
    val ownerId = uuid("owner").nullable()
}

class CatsDao(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<CatsDao>(CatsTable)
    var name by CatsTable.name.value(CatName)
    var ownerId by CatsTable.ownerId.value(Companion, OwnerId)
}

class CatName private constructor(value: String): StringValue(value) {
    companion object: NonEmptyStringValueFactory<CatName>(::CatName)
}

class OwnerId private constructor(value: UUID): UUIDValue(value) {
    companion object: UUIDValueFactory<OwnerId>(::OwnerId)
}

