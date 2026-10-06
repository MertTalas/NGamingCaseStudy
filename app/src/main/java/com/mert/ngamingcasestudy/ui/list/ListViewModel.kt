package com.mert.ngamingcasestudy.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.ngamingcasestudy.domain.model.DeletedPost
import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.usecase.DeletePostUseCase
import com.mert.ngamingcasestudy.domain.usecase.LoadPostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.ObservePostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.RefreshPostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.RestorePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ListViewModel @Inject constructor(
    observePosts: ObservePostsUseCase,
    private val loadPosts: LoadPostsUseCase,
    private val refreshPosts: RefreshPostsUseCase,
    private val deletePost: DeletePostUseCase,
    private val restorePost: RestorePostUseCase,
) : ViewModel() {

    private val posts = observePosts()
    private val loadStatus = MutableStateFlow(LoadStatus.LOADING)
    private val imagePositions = MutableStateFlow<Map<Int, Int>>(emptyMap())

    val uiState: StateFlow<ListUiState> = combine(loadStatus, posts, imagePositions) { status, posts, imagePositions ->
        when (status) {
            LoadStatus.LOADING -> ListUiState.Loading
            LoadStatus.RETRYING -> ListUiState.Error(isRetrying = true)
            LoadStatus.FAILED -> ListUiState.Error(isRetrying = false)
            LoadStatus.LOADED -> ListUiState.Success(posts.toItems(imagePositions), isRefreshing = false)
            LoadStatus.REFRESHING -> ListUiState.Success(posts.toItems(imagePositions), isRefreshing = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ListUiState.Loading)

    private val _events = Channel<ListEvent>(Channel.BUFFERED)
    val events: Flow<ListEvent> = _events.receiveAsFlow()

    private var lastDeletedPost: DeletedPost? = null

    init {
        load()
    }

    fun onRetry() {
        if (loadStatus.value != LoadStatus.FAILED) return
        viewModelScope.launch {
            loadStatus.value = LoadStatus.RETRYING
            loadStatus.value = withMinimumFeedback { loadPosts() }
                .onSuccess { rememberImagePositions() }
                .toLoadStatus()
        }
    }

    fun onRefresh() {
        if (loadStatus.value != LoadStatus.LOADED) return
        viewModelScope.launch {
            loadStatus.value = LoadStatus.REFRESHING
            val result = withMinimumFeedback { refreshPosts() }.onSuccess { rememberImagePositions() }
            loadStatus.value = LoadStatus.LOADED
            if (result.isFailure) _events.trySend(ListEvent.ShowRefreshError)
        }
    }

    fun onPostDeleted(post: Post) {
        lastDeletedPost = deletePost(post.id) ?: return
        _events.trySend(ListEvent.ShowUndoDelete)
    }

    fun onUndoDelete() {
        lastDeletedPost?.let(restorePost::invoke)
        lastDeletedPost = null
    }

    private fun load() {
        viewModelScope.launch {
            loadStatus.value = LoadStatus.LOADING
            loadStatus.value = loadPosts()
                .onSuccess { rememberImagePositions() }
                .toLoadStatus()
        }
    }

    private suspend fun rememberImagePositions() {
        imagePositions.value = posts.first().withIndex().associate { (index, post) -> post.id to index }
    }

    private fun List<Post>.toItems(imagePositions: Map<Int, Int>) = mapIndexed { index, post ->
        PostListItem(post, imagePosition = imagePositions[post.id] ?: index)
    }

    private suspend fun withMinimumFeedback(block: suspend () -> Result<Unit>): Result<Unit> = coroutineScope {
        val minimumFeedback = launch { delay(MIN_FEEDBACK_MILLIS.milliseconds) }
        block().also { result ->
            if (result.isSuccess) minimumFeedback.cancel() else minimumFeedback.join()
        }
    }

    private fun Result<Unit>.toLoadStatus() = if (isSuccess) LoadStatus.LOADED else LoadStatus.FAILED

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MIN_FEEDBACK_MILLIS = 600L
    }
}
