package com.example.projectgroup7.network

import com.example.projectgroup7.model.BookResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApiService {
    @GET("volumes")
    fun searchBooks(
        @Query("q") query: String
    ): Call<BookResponse>
}
