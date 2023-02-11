package io.andrewohara.utils.result

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success

fun <T, E> Result<T, E>.toResult4k() = when(this) {
    is Ok -> Success(value)
    is Err -> Failure(error)
}

fun <T, E> Result4k<T, E>.toKotlinResult() = when(this) {
    is Success -> Ok(value)
    is Failure -> Err(reason)
}