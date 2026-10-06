package com.mert.ngamingcasestudy.ui.detail

import androidx.lifecycle.SavedStateHandle
import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.fake.FakePostRepository
import com.mert.ngamingcasestudy.domain.usecase.ObservePostUseCase
import com.mert.ngamingcasestudy.domain.usecase.UpdatePostUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val repository = FakePostRepository(listOf(Post(2, "Title", "Body")))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(postId: Int = 2) = DetailViewModel(
        SavedStateHandle(mapOf("postId" to postId, "imagePosition" to 1)),
        ObservePostUseCase(repository),
        UpdatePostUseCase(repository),
    ).also { vm -> backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} } }

    private fun DetailViewModel.editing() = uiState.value as DetailUiState.Editing

    @Test
    fun `shows post with save disabled initially`() = runTest {
        val state = viewModel().editing()

        assertEquals("Title", state.title)
        assertEquals("Body", state.body)
        assertEquals("https://picsum.photos/300/300?random=1&grayscale", state.imageUrl)
        assertFalse(state.isSaveEnabled)
    }

    @Test
    fun `enables save only when content changes and is not blank`() = runTest {
        val viewModel = viewModel()

        viewModel.onTitleChanged("New title")
        assertTrue(viewModel.editing().isSaveEnabled)

        viewModel.onTitleChanged("Title  ")
        assertFalse(viewModel.editing().isSaveEnabled)

        viewModel.onTitleChanged(" ")
        assertFalse(viewModel.editing().isSaveEnabled)
    }

    @Test
    fun `save updates repository and emits saved event`() = runTest {
        val viewModel = viewModel()

        viewModel.onTitleChanged("New title")
        viewModel.onBodyChanged("New body ")
        viewModel.onSave()

        assertEquals(Post(2, "New title", "New body"), repository.posts.value.single())
        assertEquals(DetailEvent.Saved, viewModel.events.first())
    }

    @Test
    fun `missing post shows not found`() = runTest {
        assertEquals(DetailUiState.NotFound, viewModel(postId = 99).uiState.value)
    }

}
