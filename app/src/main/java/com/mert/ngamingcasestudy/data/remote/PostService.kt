package com.mert.ngamingcasestudy.data.remote

import retrofit2.http.GET

interface PostService {

    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
