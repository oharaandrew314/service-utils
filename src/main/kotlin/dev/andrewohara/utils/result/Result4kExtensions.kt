package dev.andrewohara.utils.result

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success

/**
 * Become a Failure if the condition is met.
 */
fun <S, E> Result<S, E>.failIf(cond: (S) -> Boolean, f: (S) -> E) = when(this) {
    is Success -> if (cond(value)) Failure(f(value)) else this
    is Failure -> this
}

/**
 * Become a Success if the condition is met.
 */
fun <S, E> Result<S, E>.recoverIf(cond: (E) -> Boolean, f: (E) -> S) = when(this) {
    is Success -> this
    is Failure -> if (cond(reason)) Success(f(reason)) else this
}
