package com.m3sv.plainupnp.upnp.navigator

import com.m3sv.plainupnp.upnp.BrowseToModel


interface UpnpNavigator {
    fun navigateHome()

    fun navigateTo(model: BrowseToModel)

    fun navigatePrevious(): Boolean
}