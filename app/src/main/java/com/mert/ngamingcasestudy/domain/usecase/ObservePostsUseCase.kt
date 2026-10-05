package com.mert.ngamingcasestudy.domain.usecase

import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePostsUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    operator fun invoke(): Flow<List<Post>> = repository.posts
}
