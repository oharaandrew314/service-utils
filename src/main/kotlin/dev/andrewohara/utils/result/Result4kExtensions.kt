package dev.andrewohara.utils.result

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.flatMap
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.valueOrNull

fun <T, E> Result<T, E>.isSuccess(): Boolean = valueOrNull() != null
fun <S> S.Success() = Success(this)

fun <S: Any, F: Any> Result4k<S, F>.peekOrFail(fn: (S) -> Result4k<Any, F>) =
    flatMap { success -> fn(success).map { success } }