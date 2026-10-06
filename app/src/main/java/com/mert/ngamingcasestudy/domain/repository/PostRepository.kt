package com.mert.ngamingcasestudy.domain.repository

import com.mert.ngamingcasestudy.domain.model.DeletedPost
import com.mert.ngamingcasestudy.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    val posts: Flow<List<Post>>

    fun observePost(id: Int): Flow<Post?>

    suspend fun loadPosts(): Result<Unit>

    suspend fun refreshPosts(): Result<Unit>

    fun updatePost(id: Int, title: String, body: String)

    fun deletePost(id: Int): DeletedPost?

    fun restorePost(deletedPost: DeletedPost)
}
