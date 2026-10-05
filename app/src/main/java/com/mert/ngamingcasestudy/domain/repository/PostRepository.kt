package com.mert.ngamingcasestudy.domain.repository

import com.mert.ngamingcasestudy.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for posts. Posts are fetched once and kept in memory;
 * edits and deletions are applied locally because the API does not persist writes.
 */
interface PostRepository {

    val posts: Flow<List<Post>>

    fun observePost(id: Int): Flow<Post?>

    /** Fetches posts if they have not been loaded yet. Safe to call again after a failure. */
    suspend fun loadPosts(): Result<Unit>

    fun updatePost(id: Int, title: String, body: String)

    fun deletePost(id: Int)
}
