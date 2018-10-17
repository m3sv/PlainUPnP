package com.m3sv.plainupnp.network

import com.m3sv.plainupnp.network.responses.TasteDiveResponse
import retrofit2.http.GET
import retrofit2.http.Query


interface TasteDiveService {
    @GET("${BuildConfig.TASTE_DIVE_BASE_URL}/similar?key=${BuildConfig.TASTE_DIVE_API_KEY}")
    fun getSuggestions(@Query(value = "q") query: String, @Query(value = "type") type: String): TasteDiveResponse
}