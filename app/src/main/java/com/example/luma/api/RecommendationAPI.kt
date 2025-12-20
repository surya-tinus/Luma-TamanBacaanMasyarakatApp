package com.example.luma.api

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// 1. Model Data dari JSON API Python
data class RecommendationResponse(
    val userId: String,
    val recommendations: List<RecommendationItem>
)

data class RecommendationItem(
    val bookId: String
)

// 2. Interface API
interface RecommendationService {
    @GET("recommend/{userId}")
    fun getRecommendations(@Path("userId") userId: String): Call<RecommendationResponse>
}

// 3. Object Singleton untuk Retrofit (Biar gampang dipanggil)
object RetrofitClient {
    // Ganti URL ini dengan URL Hugging Face kamu (Direct URL)
    // PENTING: Harus diakhiri tanda garis miring '/'
    private const val BASE_URL = "https://surya-tinus-luma-recommendationapi.hf.space/"

    val instance: RecommendationService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(RecommendationService::class.java)
    }
}