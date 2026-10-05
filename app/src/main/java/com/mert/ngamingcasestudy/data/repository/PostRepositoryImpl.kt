package com.mert.ngamingcasestudy.data.repository

import com.mert.ngamingcasestudy.data.mapper.toDomain
import com.mert.ngamingcasestudy.data.remote.PostService
import com.mert.ngamingcasestudy.di.IoDispatcher
import com.mert.ngamingcasestudy.domain.model.DeletedPost
import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.repository.PostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val service: PostService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PostRepository {

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    override val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val loadMutex = Mutex()
    private var isLoaded = false

    override fun observePost(id: Int): Flow<Post?> =
        _posts.map { posts -> posts.find { it.id == id } }.distinctUntilChanged()

    override suspend fun loadPosts(): Result<Unit> = loadMutex.withLock {
        if (isLoaded) return Result.success(Unit)
        try {
            _posts.value = withContext(ioDispatcher) {
                service.getPosts().map { it.toDomain() }
            }
            isLoaded = true
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun updatePost(id: Int, title: String, body: String) {
        _posts.update { posts ->
            posts.map { if (it.id == id) it.copy(title = title, body = body) else it }
        }
    }

    override fun deletePost(id: Int): DeletedPost? {
        var deletedPost: DeletedPost? = null
        _posts.update { posts ->
            val index = posts.indexOfFirst { it.id == id }
            deletedPost = posts.getOrNull(index)?.let { DeletedPost(it, index) }
            if (index == -1) posts else posts.toMutableList().apply { removeAt(index) }
        }
        return deletedPost
    }

    override fun restorePost(deletedPost: DeletedPost) {
        _posts.update { posts ->
            if (posts.any { it.id == deletedPost.post.id }) {
                posts
            } else {
                posts.toMutableList().apply {
                    add(deletedPost.index.coerceAtMost(size), deletedPost.post)
                }
            }
        }
    }
}
