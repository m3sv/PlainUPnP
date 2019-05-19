package com.m3sv.plainupnp.upnp

import io.reactivex.Observable


interface UpnpNavigator {
    val state: Observable<ContentState>

    fun navigateHome()

    fun navigateTo(model: BrowseToModel)

    fun navigatePrevious(): Boolean
}