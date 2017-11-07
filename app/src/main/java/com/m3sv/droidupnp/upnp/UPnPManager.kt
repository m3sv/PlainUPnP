package com.m3sv.droidupnp.upnp

import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IFactory
import java.util.*


class UPnPManager constructor(val controller: IUPnPServiceController, val factory: IFactory) : Observable() {

}