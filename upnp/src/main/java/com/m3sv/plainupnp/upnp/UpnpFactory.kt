package com.m3sv.plainupnp.upnp

import javax.inject.Inject

class UpnpFactory @Inject constructor(val upnpServiceController: UpnpServiceController) {

    fun createRendererCommand(rendererStateObservable: UpnpRendererStateObservable?): RendererCommand? =
        upnpServiceController.serviceListener
            .upnpService
            ?.controlPoint
            ?.let {
                rendererStateObservable?.let { rs ->
                    RendererCommand(
                        upnpServiceController,
                        it,
                        rs
                    )
                }
            }

    fun createContentDirectoryCommand(): ContentDirectoryCommand? = upnpServiceController
        .serviceListener
        .upnpService
        ?.controlPoint
        ?.let { ContentDirectoryCommand(it, upnpServiceController) }
}
