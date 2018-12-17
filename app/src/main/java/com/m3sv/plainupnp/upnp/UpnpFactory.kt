package com.m3sv.plainupnp.upnp

import android.content.Context
import org.droidupnp.legacy.cling.UpnpRendererStateObservable
import org.droidupnp.legacy.upnp.Factory
import javax.inject.Inject

class UpnpFactory @Inject constructor(private val controller: UpnpServiceController) : Factory {

    override fun createContentDirectoryCommand(): ContentDirectoryCommand? =
        controller.serviceListener.upnpService
            ?.controlPoint
            ?.let { ContentDirectoryCommand(it, controller) }

    override fun createRendererCommand(rendererStateObservable: UpnpRendererStateObservable?): RendererCommand? =
        controller.serviceListener.upnpService
            ?.controlPoint
            ?.let { rendererStateObservable?.let { rs -> RendererCommand(controller, it, rs) } }

    override fun createUpnpServiceController(ctx: Context): UpnpServiceController = controller

    override fun createRendererState(): UpnpRendererStateObservable =
        UpnpRendererStateObservable()
}
