package com.mert.ngamingcasestudy.ui.list

import com.mert.ngamingcasestudy.domain.model.Post
import com.mert.ngamingcasestudy.ui.common.postImageUrl

data class PostListItem(
    val post: Post,
    val imagePosition: Int,
) {
    val imageUrl: String get() = postImageUrl(imagePosition)
}
