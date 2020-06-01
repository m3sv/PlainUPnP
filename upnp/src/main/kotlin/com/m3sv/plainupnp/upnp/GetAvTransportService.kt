package com.m3sv.plainupnp.upnp

import org.fourthline.cling.model.types.UDAServiceType

class GetAvTransportService(private val serviceFinder: RendererServiceFinder) {
    private val service by lazy {
        serviceFinder.findService(UDAServiceType("AVTransport"))
    }
}
