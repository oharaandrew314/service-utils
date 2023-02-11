package io.andrewohara.utils.exposed

import io.andrewohara.utils.jdbc.TestDb
import org.jetbrains.exposed.sql.Database

fun TestDb.database() = Database.connect(this)
