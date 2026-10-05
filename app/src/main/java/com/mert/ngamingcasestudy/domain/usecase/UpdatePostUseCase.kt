package com.mert.ngamingcasestudy.domain.usecase

import com.mert.ngamingcasestudy.domain.repository.PostRepository
import javax.inject.Inject

class UpdatePostUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    operator fun invoke(id: Int, title: String, body: String) {
        repository.updatePost(id, title.trim(), body.trim())
    }
}
