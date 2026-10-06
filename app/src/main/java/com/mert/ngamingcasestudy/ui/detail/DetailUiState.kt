package com.mert.ngamingcasestudy.ui.detail

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data object NotFound : DetailUiState
    data class Editing(
        val postId: Int,
        val imageUrl: String,
        val title: String,
        val body: String,
        val isSaveEnabled: Boolean,
    ) : DetailUiState
}
