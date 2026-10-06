package com.mert.ngamingcasestudy.ui.detail

sealed interface DetailEvent {
    data object Saved : DetailEvent
}
