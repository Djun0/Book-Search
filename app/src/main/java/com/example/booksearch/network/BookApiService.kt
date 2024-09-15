package com.example.booksearch.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApiService{
    @GET("volumes")suspend fun getBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int=10
    ): QueryResult


    @GET("volumes/{volumeId}")
   suspend fun getBookById( @Path("volumeId")id:String): BookInfo
}
