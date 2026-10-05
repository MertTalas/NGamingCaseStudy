package com.mert.ngamingcasestudy.data.repository

import com.mert.ngamingcasestudy.data.remote.PostDto
import com.mert.ngamingcasestudy.data.remote.PostService
import com.mert.ngamingcasestudy.domain.model.DeletedPost
import com.mert.ngamingcasestudy.domain.model.Post
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

    private fun TestScope.repository(service: PostService) =
        PostRepositoryImpl(service, StandardTestDispatcher(testScheduler))

    private class FakePostService(private val result: () -> List<PostDto>) : PostService {
        var callCount = 0
        override suspend fun getPosts(): List<PostDto> {
            callCount++
            return result()
        }
    }

    @Test
    fun `loadPosts maps dtos to domain posts`() = runTest {
        val repository = repository(FakePostService { dtos })

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
        val repository = repository(service)

        repository.loadPosts()
        repository.loadPosts()

        assertEquals(1, service.callCount)
    }

    @Test
    fun `loadPosts returns failure and allows retry`() = runTest {
        var shouldFail = true
        val service = FakePostService { if (shouldFail) throw IOException() else dtos }
        val repository = repository(service)

        assertTrue(repository.loadPosts().isFailure)
        shouldFail = false
        assertTrue(repository.loadPosts().isSuccess)

        assertEquals(2, service.callCount)
        assertEquals(2, repository.posts.value.size)
    }

    @Test
    fun `updatePost changes only the matching post`() = runTest {
        val repository = repository(FakePostService { dtos })
        repository.loadPosts()

        repository.updatePost(id = 2, title = "Edited", body = "New body")

        assertEquals(Post(1, "First", "Body 1"), repository.posts.value[0])
        assertEquals(Post(2, "Edited", "New body"), repository.observePost(2).first())
    }

    @Test
    fun `deletePost removes the post and returns it with its index`() = runTest {
        val repository = repository(FakePostService { dtos })
        repository.loadPosts()

        val deleted = repository.deletePost(2)

        assertEquals(DeletedPost(Post(2, "Second", "Body 2"), index = 1), deleted)
        assertEquals(listOf(1), repository.posts.value.map { it.id })
        assertNull(repository.observePost(2).first())
    }

    @Test
    fun `deletePost returns null for an unknown id`() = runTest {
        val repository = repository(FakePostService { dtos })
        repository.loadPosts()

        assertNull(repository.deletePost(99))
        assertEquals(2, repository.posts.value.size)
    }

    @Test
    fun `restorePost puts the post back at its original index`() = runTest {
        val repository = repository(FakePostService { dtos })
        repository.loadPosts()
        val deleted = requireNotNull(repository.deletePost(1))

        repository.restorePost(deleted)

        assertEquals(listOf(1, 2), repository.posts.value.map { it.id })
    }

    @Test
    fun `restorePost clamps the index when the list got shorter`() = runTest {
        val repository = repository(FakePostService { dtos })
        repository.loadPosts()
        val deleted = requireNotNull(repository.deletePost(2))
        repository.deletePost(1)

        repository.restorePost(deleted)

        assertEquals(listOf(2), repository.posts.value.map { it.id })
    }

    @Test
    fun `restorePost ignores a post that is already in the list`() = runTest {
        val repository = repository(FakePostService { dtos })
        repository.loadPosts()
        val deleted = requireNotNull(repository.deletePost(1))

        repository.restorePost(deleted)
        repository.restorePost(deleted)

        assertEquals(listOf(1, 2), repository.posts.value.map { it.id })
    }
}
