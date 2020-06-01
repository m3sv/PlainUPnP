package com.m3sv.plainupnp.upnp.actions

import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.meta.Service
import timber.log.Timber

abstract class AvAction<A, T>(private val controlPoint: ControlPoint) {

    protected val tag = "AV_ACTION"

    abstract suspend operator fun invoke(service: Service<*, *>, vararg arguments: A): T

    protected fun executeAVAction(callback: ActionCallback) {
        try {
            controlPoint.execute(callback)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}
