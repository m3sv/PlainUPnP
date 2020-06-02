package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.RendererServiceFinder
import org.fourthline.cling.model.types.UDAServiceType
import javax.inject.Inject

class AvServiceFinder @Inject constructor(private val serviceFinder: RendererServiceFinder) {

    fun getService() = serviceFinder.findService(UDAServiceType("AVTransport"))

}
