package com.mert.ngamingcasestudy.ui.list

sealed interface ListUiState {
    data object Loading : ListUiState
    data class Error(val isRetrying: Boolean) : ListUiState
    data class Success(val items: List<PostListItem>, val isRefreshing: Boolean) : ListUiState
}
