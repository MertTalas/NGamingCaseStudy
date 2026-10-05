package com.mert.ngamingcasestudy.domain.usecase

import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePostUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    operator fun invoke(id: Int): Flow<Post?> = repository.observePost(id)
}
