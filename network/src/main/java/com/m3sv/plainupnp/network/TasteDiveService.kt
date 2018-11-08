package com.m3sv.plainupnp.network

import com.m3sv.plainupnp.network.responses.TasteDiveResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


interface TasteDiveService {
    @GET("similar?key=${BuildConfig.TASTE_DIVE_API_KEY}")
    fun getSuggestions(@Query(value = "q") query: String, @Query(value = "type") type: String): Single<TasteDiveResponse>
}