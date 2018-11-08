package com.m3sv.plainupnp.network

import com.m3sv.plainupnp.network.responses.TasteDiveResponse
import io.reactivex.Single


interface ApiManager {

    /**
     * @param type specifies the desired type of results.
     * It can be one of the following: music, movies, shows, books, authors, games.
     * If not specified, the results can have mixed types.
     */
    fun getSuggestions(query: String, type: String): Single<TasteDiveResponse>
}

