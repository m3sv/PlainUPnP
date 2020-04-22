package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.RendererServiceFinder
import org.fourthline.cling.UpnpService
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import timber.log.Timber

abstract class RenderingAction(
    private val upnpService: UpnpService,
    private val serviceFinder: RendererServiceFinder
) {
    protected fun executeRenderingAction(callback: Service<*, *>.() -> ActionCallback) {
        serviceFinder
            .findService(UDAServiceType("RenderingControl"))
            ?.let { service ->
                try {
                    upnpService.controlPoint.execute(callback(service))
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
    }
}
