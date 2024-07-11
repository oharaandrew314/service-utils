package dev.andrewohara.utils.exposed

import dev.andrewohara.utils.jdbc.TestDb
import org.jetbrains.exposed.sql.Database

fun TestDb.database() = Database.connect(this)
