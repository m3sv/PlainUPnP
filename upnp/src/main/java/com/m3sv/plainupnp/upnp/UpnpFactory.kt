package com.m3sv.plainupnp.upnp

import javax.inject.Inject

class UpnpFactory @Inject constructor(
        override val upnpServiceController: UpnpServiceController)
    : Factory {
    override fun createRendererCommand(rendererStateObservable: UpnpRendererStateObservable?): RendererCommand? = upnpServiceController.serviceListener
            .upnpService
            ?.controlPoint
            ?.let { rendererStateObservable?.let { rs -> RendererCommand(upnpServiceController, it, rs) } }


    override fun createContentDirectoryCommand(): ContentDirectoryCommand? = upnpServiceController
            .serviceListener
            .upnpService
            ?.controlPoint
            ?.let { ContentDirectoryCommand(it, upnpServiceController) }

    override fun createRendererState(): UpnpRendererStateObservable = UpnpRendererStateObservable()
}
