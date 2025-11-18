package com.skash.forge.domain.usecase

import com.skash.forge.domain.error.ErrorType
import com.skash.forge.domain.model.DummyPost
import com.skash.forge.domain.repository.DummyRepository
import com.skash.forge.outcome.Outcome
import com.skash.forge.usecase.OutcomeUseCase
import kotlinx.coroutines.flow.FlowCollector

class GetDummyPostsUseCase(
    private val dummyRepository: DummyRepository
) : OutcomeUseCase<Unit, List<DummyPost>, ErrorType>() {
    override suspend fun FlowCollector<Outcome<List<DummyPost>, ErrorType>>.execute(
        params: Unit
    ) {
        val response = dummyRepository.fetchDummyPosts()

        emitFrom(response) { ErrorType.PostFetchFailed }
    }
}