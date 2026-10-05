package com.mert.ngamingcasestudy.domain.usecase

import com.mert.ngamingcasestudy.domain.model.DeletedPost
import com.mert.ngamingcasestudy.domain.repository.PostRepository
import javax.inject.Inject

class RestorePostUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    operator fun invoke(deletedPost: DeletedPost) = repository.restorePost(deletedPost)
}
