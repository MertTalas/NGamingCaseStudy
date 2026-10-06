package com.mert.ngamingcasestudy.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.domain.usecase.ObservePostUseCase
import com.mert.ngamingcasestudy.domain.usecase.UpdatePostUseCase
import com.mert.ngamingcasestudy.ui.common.postImageUrl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observePost: ObservePostUseCase,
    private val updatePost: UpdatePostUseCase,
) : ViewModel() {

    private val args = DetailFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val imageUrl = postImageUrl(args.imagePosition)
    private val draft = MutableStateFlow<PostDraft?>(null)

    val uiState: StateFlow<DetailUiState> = combine(observePost(args.postId), draft) { post, draft ->
        post?.toEditingState(draft ?: PostDraft(post.title, post.body)) ?: DetailUiState.NotFound
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), DetailUiState.Loading)

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events: Flow<DetailEvent> = _events.receiveAsFlow()

    fun onTitleChanged(title: String) = editDraft { it.copy(title = title) }

    fun onBodyChanged(body: String) = editDraft { it.copy(body = body) }

    fun onSave() {
        val state = uiState.value as? DetailUiState.Editing ?: return
        if (!state.isSaveEnabled) return
        updatePost(state.postId, state.title, state.body)
        _events.trySend(DetailEvent.Saved)
    }

    private fun editDraft(edit: (PostDraft) -> PostDraft) {
        val state = uiState.value as? DetailUiState.Editing ?: return
        draft.update { edit(it ?: PostDraft(state.title, state.body)) }
    }

    private fun Post.toEditingState(draft: PostDraft) = DetailUiState.Editing(
        postId = id,
        imageUrl = imageUrl,
        title = draft.title,
        body = draft.body,
        isSaveEnabled = draft.isValid() && draft.differsFrom(this),
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
