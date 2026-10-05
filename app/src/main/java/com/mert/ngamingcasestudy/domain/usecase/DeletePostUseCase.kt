package com.mert.ngamingcasestudy.domain.usecase

import com.mert.ngamingcasestudy.domain.repository.PostRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    operator fun invoke(id: Int) = repository.deletePost(id)
}
