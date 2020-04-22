package com.m3sv.plainupnp.upnp

import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.ServiceType

interface RendererServiceFinder {
    fun findService(type: ServiceType): Service<*, *>?
}
