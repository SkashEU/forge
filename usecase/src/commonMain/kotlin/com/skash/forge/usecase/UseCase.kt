package com.skash.forge.usecase

import com.skash.forge.outcome.ResultOutcome
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

abstract class UseCase<in Params, Output : Any, Error : Any>(
    private val dispatcher: CoroutineDispatcher = defaultDispatcher
) {

    protected interface UseCaseScope<Error> {
        fun raise(error: Error): Nothing

        fun ensure(condition: Boolean, error: () -> Error) {
            if (!condition) raise(error())
        }

        suspend fun <T> catch(
            block: suspend () -> T,
            mapper: (Throwable) -> Error
        ): T
    }

    private class UseCaseScopeImpl<Error> : UseCaseScope<Error> {
        override fun raise(error: Error): Nothing {
            throw UseCaseFailureException(error)
        }

        override suspend fun <T> catch(
            block: suspend () -> T,
            mapper: (Throwable) -> Error
        ): T {
            return try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (t: Throwable) {
                raise(mapper(t))
            }
        }
    }

    private class UseCaseFailureException(val error: Any?) : RuntimeException()

    protected abstract suspend fun UseCaseScope<Error>.execute(params: Params): Output

    protected abstract fun mapError(t: Throwable): Error

    suspend operator fun invoke(params: Params): ResultOutcome<Output, Error> {
        return withContext(dispatcher) {
            try {
                val result = UseCaseScopeImpl<Error>().execute(params)
                ResultOutcome.Success(result)
            } catch (e: UseCaseFailureException) {
                // This is safe because its a internal class. We only need this as any due to type erasure
                @Suppress("UNCHECKED_CAST")
                ResultOutcome.Failure(e.error as Error)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                ResultOutcome.Failure(mapError(e))
            }
        }
    }
}

suspend operator fun <Output: Any, Error : Any> UseCase<Unit, Output, Error>.invoke(): ResultOutcome<Output, Error> = invoke(Unit)