package dev.andrewohara.utils

import dev.andrewohara.utils.jdk.nexBase64
import dev.andrewohara.utils.jdk.nextBase36
import dev.andrewohara.utils.jdk.nextBase62
import dev.andrewohara.utils.jdk.nextBytes
import dev.andrewohara.utils.jdk.nextHex
import dev.andrewohara.utils.jdk.nextUuid
import java.util.Random
import kotlin.random.asJavaRandom

open class IdGenerator(private val random: Random = Random()) {

    constructor(random: kotlin.random.Random): this(random.asJavaRandom())

    companion object: IdGenerator()

    fun nexBase64(bytes: Int, urlSafe: Boolean = false) = random.nexBase64(bytes, urlSafe)
    fun nextBase62(length: Int) = random.nextBase62(length)
    fun nextBase36(length: Int) = random.nextBase36(length)
    fun nextHex(length: Int, uppercase: Boolean = true) = random.nextHex(length, uppercase)

    fun nextBytes(length: Int) = random.nextBytes(length)

    fun nextUuid() = random.nextUuid()
}