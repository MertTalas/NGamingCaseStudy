package com.mert.ngamingcasestudy.fake

import com.mert.ngamingcasestudy.domain.model.DeletedPost
import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePostRepository(
    initial: List<Post> = emptyList(),
    var loadResult: () -> Result<Unit> = { Result.success(Unit) },
) : PostRepository {

    override val posts = MutableStateFlow(initial)

    var loadCount = 0
        private set

    override fun observePost(id: Int): Flow<Post?> = posts.map { list -> list.find { it.id == id } }

    override suspend fun loadPosts(): Result<Unit> {
        loadCount++
        return loadResult()
    }

    override fun updatePost(id: Int, title: String, body: String) {
        posts.update { list -> list.map { if (it.id == id) it.copy(title = title, body = body) else it } }
    }

    override fun deletePost(id: Int): DeletedPost? = null

    override fun restorePost(deletedPost: DeletedPost) = Unit
}
