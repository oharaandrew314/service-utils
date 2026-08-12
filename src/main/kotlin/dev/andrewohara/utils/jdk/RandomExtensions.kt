package dev.andrewohara.utils.jdk

import java.util.Base64
import java.util.Random
import java.util.UUID

private val base62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()
private val base36 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
private val hex = "0123456789ABCDEF".toCharArray()

private val base64Encoder = Base64.getEncoder()
private val base64UrlEncoder = Base64.getUrlEncoder()

private fun Random.next(length: Int, chars: CharArray): String {
    val sb = StringBuilder(length)
    repeat(length) {
        sb.append(chars[nextInt(chars.size - 1)])
    }
    return sb.toString()
}

fun Random.nexBase64(bytes: Int, urlSafe: Boolean = false): String = nextBytes(bytes).let {
    (if (urlSafe) base64UrlEncoder else base64Encoder).encodeToString(it)
}

fun Random.nextBase62(length: Int) = next(length, base62)

fun Random.nextBase36(length: Int) = next(length, base36)

fun Random.nextHex(length: Int, uppercase: Boolean) = next(length, hex)
    .let { if (uppercase) it else it.lowercase() }

fun Random.nextBytes(length: Int) = ByteArray(length).also(::nextBytes)

fun Random.nextUuid() = UUID(nextLong(), nextLong())
