package com.m3sv.plainupnp.upnp

interface UpnpNavigator {

    fun navigateTo(destination: Destination)

}

sealed class Destination {
    object Home : Destination()
    object Back : Destination()
    data class Path(val id: String, val directoryName: String) : Destination()
}