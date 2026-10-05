package com.mert.ngamingcasestudy.domain.usecase

import com.mert.ngamingcasestudy.domain.repository.PostRepository
import javax.inject.Inject

class LoadPostsUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(): Result<Unit> = repository.loadPosts()
}
