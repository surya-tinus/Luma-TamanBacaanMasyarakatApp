package com.example.luma.network

import com.example.luma.model.BookResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BooksApiService {
    @GET("volumes")
    fun searchBooks(
        @Query("q") query: String,
        @Query("orderBy") orderBy: String? = null
    ): Call<BookResponse>

}
