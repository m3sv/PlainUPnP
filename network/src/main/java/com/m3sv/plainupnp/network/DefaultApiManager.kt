package com.m3sv.plainupnp.network

import com.m3sv.plainupnp.network.responses.BaseResponse
import com.m3sv.plainupnp.network.responses.OmdbResponse
import com.m3sv.plainupnp.network.responses.TasteDiveResponse
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


class DefaultApiManager @Inject constructor(
    private val tasteDiveService: TasteDiveService,
    private val omdbService: OmdbService
) : ApiManager {

    override fun getSuggestions(query: String, type: String): Single<TasteDiveResponse> =
        tasteDiveService.getSuggestions(query, type).compose(applySingleBgToFgTransformers())

    override fun getMovieInfo(query: String): Single<OmdbResponse> =
        omdbService.getMovieInfo(query).compose(applySingleBgToFgTransformers())

    private fun <T : BaseResponse> applySingleBgToFgTransformers(): SingleTransformer<T, T> =
        SingleTransformer {
            it.compose(applySingleBgToFgSchedulers())
        }

    private fun <T> applySingleBgToFgSchedulers(): SingleTransformer<T, T> = SingleTransformer {
        it.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
    }
}