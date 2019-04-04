package com.m3sv.plainupnp.upnp.navigator

import com.m3sv.plainupnp.upnp.BrowseToModel
import com.m3sv.plainupnp.upnp.ContentState
import io.reactivex.Observable


interface UpnpNavigator {
    val state: Observable<ContentState>

    fun navigateHome()

    fun navigateTo(model: BrowseToModel)

    fun navigatePrevious(): Boolean
}