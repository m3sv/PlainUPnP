package com.m3sv.plainupnp.upnp.store

sealed class ContentState {
    object Loading : ContentState()
    data class Success(val upnpDirectory: UpnpDirectory) : ContentState()
}
