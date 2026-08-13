package dev.andrewohara.utils.monads

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import dev.forkhandles.result4k.map

sealed interface Update<out T> {

    fun <O : Any> map(f: (T & Any) -> O): Update<O?> = when (this) {
        is Retain -> Retain()
        is Set -> if (value == null) Set(null) else Set(f(value))
    }

    fun <O : Any, E : Any> flatMap(f: (T & Any) -> Result4k<O, E>): Result4k<Update<O?>, E> = when (this) {
        is Retain -> Retain<O>().asSuccess()
        is Set -> if (value == null) Set(null).asSuccess() else f(value).map { Set(it) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <S : @UnsafeVariance T> effective(original: S): S = when (this) {
        is Retain -> original
        is Set -> value as S
    }

    fun orNull() = when(this) {
        is Retain -> null
        is Set -> value
    }

    fun ifSet(f: (T) -> Unit) = when(this) {
        is Retain -> Unit
        is Set -> f(value)
    }

    class Retain<T>: Update<T>
    class Set<out T>(val value: T): Update<T>

    companion object {
        operator fun <T: Any> invoke(value: T) = Set(value) as Update<T>
        @JvmName("nullary")
        operator fun <T: Any> invoke(value: T?) = Set(value) as Update<T?>
        operator fun <T: Any> invoke() = Retain<T>() as Update<T>
    }
}