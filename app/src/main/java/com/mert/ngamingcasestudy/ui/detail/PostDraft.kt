package com.mert.ngamingcasestudy.ui.detail

import com.mert.ngamingcasestudy.domain.model.Post

data class PostDraft(
    val title: String,
    val body: String,
) {
    fun isValid() = title.isNotBlank() && body.isNotBlank()

    fun differsFrom(post: Post) = title.trim() != post.title || body.trim() != post.body
}
