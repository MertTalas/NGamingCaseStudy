package com.mert.ngamingcasestudy.ui.list

import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.usecase.DeletePostUseCase
import com.mert.ngamingcasestudy.domain.usecase.LoadPostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.ObservePostsUseCase
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

        assertEquals(ListUiState.Success(posts), viewModel.uiState.value)
    }

    @Test
    fun `retry is ignored while a retry is in progress`() = runTest {
        val viewModel = viewModel()

        viewModel.onRetry()
        viewModel.onRetry()

        assertEquals(2, repository.loadCount)
    }
}
