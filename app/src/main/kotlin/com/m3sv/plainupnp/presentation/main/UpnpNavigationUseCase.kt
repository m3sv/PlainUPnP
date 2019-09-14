package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.upnp.Destination
import com.m3sv.plainupnp.upnp.UpnpNavigator
import javax.inject.Inject

class UpnpNavigationUseCase @Inject constructor(private val upnpNavigator: UpnpNavigator) {
    fun execute(destination: Destination) = upnpNavigator.navigateTo(destination)
}