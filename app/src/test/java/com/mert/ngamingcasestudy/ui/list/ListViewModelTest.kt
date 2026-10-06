package com.mert.ngamingcasestudy.ui.list

import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.usecase.DeletePostUseCase
import com.mert.ngamingcasestudy.domain.usecase.LoadPostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.ObservePostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.RefreshPostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.RestorePostUseCase
import com.mert.ngamingcasestudy.fake.FakePostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    private val posts = listOf(Post(1, "Title", "Body"))
    private val failure = { Result.failure<Unit>(IOException()) }
    private val repository = FakePostRepository(posts, loadResult = failure)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel() = ListViewModel(
        ObservePostsUseCase(repository),
        LoadPostsUseCase(repository),
        RefreshPostsUseCase(repository),
        DeletePostUseCase(repository),
        RestorePostUseCase(repository),
    ).also { vm -> backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} } }

    @Test
    fun `failed retry keeps error screen and shows progress for minimum duration`() = runTest {
        val viewModel = viewModel()
        assertEquals(ListUiState.Error(isRetrying = false), viewModel.uiState.value)

        viewModel.onRetry()
        assertEquals(ListUiState.Error(isRetrying = true), viewModel.uiState.value)

        advanceTimeBy(599)
        assertEquals(ListUiState.Error(isRetrying = true), viewModel.uiState.value)

        advanceTimeBy(2)
        assertEquals(ListUiState.Error(isRetrying = false), viewModel.uiState.value)
    }

    @Test
    fun `successful retry shows posts without waiting`() = runTest {
        val viewModel = viewModel()
        repository.loadResult = { Result.success(Unit) }

        viewModel.onRetry()
        runCurrent()

        assertEquals(ListUiState.Success(posts.asItems(), isRefreshing = false), viewModel.uiState.value)
    }

    @Test
    fun `retry is ignored while a retry is in progress`() = runTest {
        val viewModel = viewModel()

        viewModel.onRetry()
        viewModel.onRetry()

        assertEquals(2, repository.loadCount)
    }

    @Test
    fun `refresh shows refreshing state and reloads posts`() = runTest {
        repository.loadResult = { Result.success(Unit) }
        val viewModel = viewModel()
        viewModel.onPostDeleted(posts.single())
        assertEquals(ListUiState.Success(emptyList(), isRefreshing = false), viewModel.uiState.value)

        viewModel.onRefresh()
        runCurrent()

        assertEquals(ListUiState.Success(posts.asItems(), isRefreshing = false), viewModel.uiState.value)
        assertEquals(1, repository.refreshCount)
    }

    @Test
    fun `failed refresh keeps posts and shows refresh error`() = runTest {
        repository.loadResult = { Result.success(Unit) }
        val viewModel = viewModel()
        repository.loadResult = failure
        val events = mutableListOf<ListEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.collect(events::add) }

        viewModel.onRefresh()
        assertEquals(ListUiState.Success(posts.asItems(), isRefreshing = true), viewModel.uiState.value)

        advanceTimeBy(601)
        assertEquals(ListUiState.Success(posts.asItems(), isRefreshing = false), viewModel.uiState.value)
        assertEquals(listOf(ListEvent.ShowRefreshError), events)
    }

    @Test
    fun `refresh is ignored while the error screen is shown`() = runTest {
        val viewModel = viewModel()

        viewModel.onRefresh()

        assertEquals(0, repository.refreshCount)
    }

    @Test
    fun `remaining posts keep their images after a delete`() = runTest {
        val (first, second, third) = loadedThreePosts()
        val viewModel = viewModel()

        viewModel.onPostDeleted(first)

        assertEquals(
            listOf(PostListItem(second, imagePosition = 1), PostListItem(third, imagePosition = 2)),
            viewModel.items(),
        )
    }

    @Test
    fun `undo puts the post back at its index with its image`() = runTest {
        val threePosts = loadedThreePosts()
        val viewModel = viewModel()

        viewModel.onPostDeleted(threePosts[1])
        viewModel.onUndoDelete()

        assertEquals(threePosts.asItems(), viewModel.items())
    }

    @Test
    fun `refresh assigns images by the new load order`() = runTest {
        val (first, second, third) = loadedThreePosts()
        val viewModel = viewModel()
        viewModel.onPostDeleted(first)
        repository.remotePosts = listOf(third, second)

        viewModel.onRefresh()
        runCurrent()

        assertEquals(
            listOf(PostListItem(third, imagePosition = 0), PostListItem(second, imagePosition = 1)),
            viewModel.items(),
        )
    }

    private fun loadedThreePosts(): List<Post> {
        val threePosts = listOf(Post(1, "One", "Body"), Post(2, "Two", "Body"), Post(3, "Three", "Body"))
        repository.remotePosts = threePosts
        repository.posts.value = threePosts
        repository.loadResult = { Result.success(Unit) }
        return threePosts
    }

    private fun ListViewModel.items() = (uiState.value as ListUiState.Success).items

    private fun List<Post>.asItems() = mapIndexed { index, post -> PostListItem(post, imagePosition = index) }
}
