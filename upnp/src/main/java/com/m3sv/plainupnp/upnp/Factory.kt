package com.m3sv.plainupnp.upnp

interface Factory {
    val upnpServiceController: UpnpServiceController

    fun createContentDirectoryCommand(): ContentDirectoryCommand?

    fun createRendererState(): UpnpRendererStateObservable

    fun createRendererCommand(rendererStateObservable: UpnpRendererStateObservable?): RendererCommand?
}
