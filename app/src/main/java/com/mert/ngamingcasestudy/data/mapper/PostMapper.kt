package com.mert.ngamingcasestudy.data.mapper

import com.mert.ngamingcasestudy.data.remote.PostDto
import com.mert.ngamingcasestudy.domain.model.Post

fun PostDto.toDomain(): Post = Post(
    id = id,
    title = title,
    body = body,
)
