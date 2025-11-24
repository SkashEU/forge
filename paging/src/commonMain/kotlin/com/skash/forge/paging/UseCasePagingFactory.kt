package com.skash.forge.paging

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first

object UseCasePagingFactory {

    fun <K : Any, P : Any, S : Any, T : Any, E> create(
        config: PagingConfig,
        startingKey: K,
        useCase: OutcomeUseCase<P, S, E>,
        paramsFactory: (key: K?, loadSize: Int) -> P,
        getItems: (successData: S) -> List<T>,
        getNextKey: (successData: S, currentKey: K) -> K?,
        getPrevKey: (successData: S, currentKey: K) -> K? = { _, _ -> null },
        transformError: Outcome.Failure<E>.() -> String,
    ): Pager<K, T> = Pager(
        config = config,
        pagingSourceFactory = {
            GenericUseCasePagingSource(
                startingKey = startingKey,
                useCase = useCase,
                paramsFactory = paramsFactory,
                getItems = getItems,
                getNextKey = getNextKey,
                getPrevKey = getPrevKey,
                transformError = transformError
            )
        }
    )
}

class GenericUseCasePagingSource<Key : Any, Params, S : Any, T : Any, E>(
    private val startingKey: Key,
    private val useCase: OutcomeUseCase<Params, S, E>,
    private val paramsFactory: (key: Key?, loadSize: Int) -> Params,
    private val getItems: (successData: S) -> List<T>,
    private val getNextKey: (successData: S, currentKey: Key) -> Key?,
    private val getPrevKey: (successData: S, currentKey: Key) -> Key?,
    private val transformError: Outcome.Failure<E>.() -> String,
) : PagingSource<Key, T>() {
    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, T> {
        val currentKey = params.key ?: startingKey
        val useCaseParams = paramsFactory(currentKey, params.loadSize)

        return runCatching {
            useCase.invoke(useCaseParams)
                .filterNot { it is Outcome.Progress }
                .first()
        }.fold(
            onSuccess = { outcome ->
                when (outcome) {
                    is Outcome.Failure -> LoadResult.Error(UseCasePagingException(transformError(outcome)))
                    is Outcome.Progress -> LoadResult.Error(UseCasePagingException("Illegal State"))
                    is Outcome.Success -> {
                        val data = outcome.data
                        val items = getItems(data)
                        LoadResult.Page(
                            data = items,
                            prevKey = getPrevKey(data, currentKey),
                            nextKey = getNextKey(data, currentKey),
                        )
                    }
                }

            },
            onFailure = {
                LoadResult.Error(it)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Key, T>): Key? = state.anchorPosition?.let { anchorPosition ->
        state.closestPageToPosition(anchorPosition)?.prevKey ?: state.closestPageToPosition(anchorPosition)?.nextKey
    }
}

class UseCasePagingException(
    error: String,
) : Exception(error)