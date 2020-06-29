package com.m3sv.plainupnp.upnp.actions

import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Service

abstract class Action<A, T>(val controlPoint: ControlPoint) {

    abstract suspend operator fun invoke(
        service: Service<*, *>,
        vararg arguments: A
    ): T
}
