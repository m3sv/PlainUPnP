package com.m3sv.plainupnp.network.responses

import com.squareup.moshi.Json


data class TasteDiveResponse(@Json(name = "Similar") val similar: Similar)

data class Similar(@Json(name = "Info") val info: List<Result>, @Json(name = "Info") val results: List<Result>)

data class Result(
    val name: String,
    val type: String,
    val teaser: String,
    val wikiUrl: String,
    val youtubeUrl: String,
    val youtubeId: String
)




