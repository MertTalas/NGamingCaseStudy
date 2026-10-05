package com.mert.ngamingcasestudy.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.ngamingcasestudy.domain.usecase.DeletePostUseCase
import com.mert.ngamingcasestudy.domain.usecase.LoadPostsUseCase
import com.mert.ngamingcasestudy.domain.usecase.ObservePostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    observePosts: ObservePostsUseCase,
    private val loadPosts: LoadPostsUseCase,
    private val deletePost: DeletePostUseCase,
) : ViewModel() {

    private enum class LoadStatus { LOADING, LOADED, FAILED }

    private val loadStatus = MutableStateFlow(LoadStatus.LOADING)

    val uiState: StateFlow<ListUiState> = combine(loadStatus, observePosts()) { status, posts ->
        when (status) {
            LoadStatus.LOADING -> ListUiState.Loading
            LoadStatus.FAILED -> ListUiState.Error
            LoadStatus.LOADED -> ListUiState.Success(posts)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), ListUiState.Loading)

    init {
        load()
    }

    fun onRetry() = load()

    fun onPostSwiped(postId: Int) = deletePost(postId)

    private fun load() {
        viewModelScope.launch {
            loadStatus.value = LoadStatus.LOADING
            loadStatus.value = if (loadPosts().isSuccess) LoadStatus.LOADED else LoadStatus.FAILED
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
