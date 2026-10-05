package com.mert.ngamingcasestudy.data.repository

import com.mert.ngamingcasestudy.data.remote.PostDto
import com.mert.ngamingcasestudy.data.remote.PostService
import com.mert.ngamingcasestudy.domain.model.Post
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class PostRepositoryImplTest {

    private val dtos = listOf(
        PostDto(userId = 1, id = 1, title = "First", body = "Body 1"),
        PostDto(userId = 1, id = 2, title = "Second", body = "Body 2"),
    )

    private class FakePostService(private val result: () -> List<PostDto>) : PostService {
        var callCount = 0
        override suspend fun getPosts(): List<PostDto> {
            callCount++
            return result()
        }
    }

    @Test
    fun `loadPosts maps dtos to domain posts`() = runTest {
        val repository = PostRepositoryImpl(FakePostService { dtos })

        val result = repository.loadPosts()

        assertTrue(result.isSuccess)
        assertEquals(
            listOf(Post(1, "First", "Body 1"), Post(2, "Second", "Body 2")),
            repository.posts.value,
        )
    }

    @Test
    fun `loadPosts fetches only once after success`() = runTest {
        val service = FakePostService { dtos }
        val repository = PostRepositoryImpl(service)

        repository.loadPosts()
        repository.loadPosts()

        assertEquals(1, service.callCount)
    }

    @Test
    fun `loadPosts returns failure and allows retry`() = runTest {
        var shouldFail = true
        val service = FakePostService { if (shouldFail) throw IOException() else dtos }
        val repository = PostRepositoryImpl(service)

        assertTrue(repository.loadPosts().isFailure)
        shouldFail = false
        assertTrue(repository.loadPosts().isSuccess)

        assertEquals(2, service.callCount)
        assertEquals(2, repository.posts.value.size)
    }

    @Test
    fun `updatePost changes only the matching post`() = runTest {
        val repository = PostRepositoryImpl(FakePostService { dtos })
        repository.loadPosts()

        repository.updatePost(id = 2, title = "Edited", body = "New body")

        assertEquals(Post(1, "First", "Body 1"), repository.posts.value[0])
        assertEquals(Post(2, "Edited", "New body"), repository.observePost(2).first())
    }

    @Test
    fun `deletePost removes the post`() = runTest {
        val repository = PostRepositoryImpl(FakePostService { dtos })
        repository.loadPosts()

        repository.deletePost(1)

        assertEquals(listOf(2), repository.posts.value.map { it.id })
        assertNull(repository.observePost(1).first())
    }
}
