package com.mert.ngamingcasestudy.ui.list

sealed interface ListEvent {
    data object ShowUndoDelete : ListEvent
}
