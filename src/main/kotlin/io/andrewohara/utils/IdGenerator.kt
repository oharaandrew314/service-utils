package io.andrewohara.utils

import java.util.Random

open class IdGenerator(private val random: Random = Random()) {

    companion object: IdGenerator()

    private val base36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    private val numeric = "0123456789".toCharArray()
    private val hex = "0123456789ABCDEF".toCharArray()

    private fun next(length: Int, chars: CharArray): String {
        val sb = StringBuilder(length)
        repeat(length) {
            sb.append(chars[random.nextInt(chars.size - 1)])
        }
        return sb.toString()
    }

    fun nextBase36(length: Int) = next(length, base36)

    fun nextNumeric(length: Int) = next(length, numeric).toBigInteger()

    fun nextHex(length: Int) = next(length, hex)
}