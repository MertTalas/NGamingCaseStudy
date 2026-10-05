package com.mert.ngamingcasestudy.ui.list

import com.mert.ngamingcasestudy.domain.model.Post

sealed interface ListUiState {
    data object Loading : ListUiState
    data object Error : ListUiState
    data class Success(val posts: List<Post>) : ListUiState
}
