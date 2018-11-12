package com.m3sv.plainupnp.network

import com.m3sv.plainupnp.network.responses.OmdbResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query


interface OmdbService {
    @GET("?apiKey=${BuildConfig.OMDB_API_KEY}")
    fun getMovieInfo(@Query("t") name: String): Single<OmdbResponse>
}