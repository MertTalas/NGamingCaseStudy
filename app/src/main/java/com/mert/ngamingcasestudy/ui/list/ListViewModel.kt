package com.mert.ngamingcasestudy.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.ngamingcasestudy.domain.model.DeletedPost
import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.usecase.DeletePostUseCase
import com.mert.ngamingcasestudy.domain.usecase.LoadPostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.ObservePostsUseCase
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class ListViewModel @Inject constructor(
    observePosts: ObservePostsUseCase,
    private val loadPosts: LoadPostsUseCase,
    private val deletePost: DeletePostUseCase,
    private val restorePost: RestorePostUseCase,
) : ViewModel() {

    private val loadStatus = MutableStateFlow(LoadStatus.LOADING)

    val uiState: StateFlow<ListUiState> = combine(loadStatus, observePosts()) { status, posts ->
        when (status) {
            LoadStatus.LOADING -> ListUiState.Loading
            LoadStatus.RETRYING -> ListUiState.Error(isRetrying = true)
            LoadStatus.FAILED -> ListUiState.Error(isRetrying = false)
            LoadStatus.LOADED -> ListUiState.Success(posts)
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
            loadStatus.value = retryLoad().toLoadStatus()
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
            loadStatus.value = loadPosts().toLoadStatus()
        }
    }

    private suspend fun retryLoad(): Result<Unit> = coroutineScope {
        val minimumFeedback = launch { delay(MIN_RETRY_FEEDBACK_MILLIS.milliseconds) }
        loadPosts().also { result ->
            if (result.isSuccess) minimumFeedback.cancel() else minimumFeedback.join()
        }
    }

    private fun Result<Unit>.toLoadStatus() = if (isSuccess) LoadStatus.LOADED else LoadStatus.FAILED

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
        const val MIN_RETRY_FEEDBACK_MILLIS = 600L
    }
}
