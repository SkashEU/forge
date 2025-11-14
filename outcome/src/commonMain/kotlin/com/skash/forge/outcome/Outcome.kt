package com.skash.forge.outcome

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull

sealed class Outcome<out S : Any, out E> {
    data class Progress(
        val message: String? = null,
    ) : Outcome<Nothing, Nothing>()

    data class Success<out S : Any>(
        val data: S,
    ) : Outcome<S, Nothing>()

    data class Failure<out E>(
        val error: E,
    ) : Outcome<Nothing, E>()

    companion object {
        fun <S : Any, E> progress(message: String? = null): Outcome<S, E> = Progress(message)

        fun <S : Any, E> success(data: S): Outcome<S, E> = Success(data)

        fun <S : Any, E> failure(error: E): Outcome<S, E> = Failure(error)
    }
}

fun <S : Any, E, H : Any> Outcome<S, E>.mapData(block: (input: S) -> H): Outcome<H, E> =
    when (this) {
        is Outcome.Success -> Outcome.success(block(data))
        is Outcome.Failure -> Outcome.failure(error)
        is Outcome.Progress -> Outcome.progress(message)
    }

/**
 * Represents a terminal result of an operation — either [Success] or [Failure].
 *
 * This class is defined separately from [Outcome] because SKIE currently cannot generate
 * a clean structure when a sealed class contains another sealed class.
 *
 * If we used a nested sealed structure, the generated code would include an additional
 * case for the inner sealed class, making it hard to read.
 *
 * We use this flattened approach to avoid nested switching logic in the generated interop code.
 */
sealed class ResultOutcome<out S : Any, out E : Any> {
    data class Success<out S : Any>(
        val data: S,
    ) : ResultOutcome<S, Nothing>()

    data class Failure<out E : Any>(
        val error: E,
    ) : ResultOutcome<Nothing, E>()
}

suspend fun <S : Any, E : Any> Flow<Outcome<S, E>>.firstResult(): ResultOutcome<S, E> =
    firstResultOrNull() ?: throw NoSuchElementException("Flow was empty.")

suspend fun <S : Any, E : Any> Flow<Outcome<S, E>>.firstResultOrNull(): ResultOutcome<S, E>? =
    when (
        val result = filter { it is Outcome.Success || it is Outcome.Failure }.firstOrNull()
    ) {
        is Outcome.Success -> ResultOutcome.Success(result.data)
        is Outcome.Failure -> ResultOutcome.Failure(result.error)
        else -> null
    }
