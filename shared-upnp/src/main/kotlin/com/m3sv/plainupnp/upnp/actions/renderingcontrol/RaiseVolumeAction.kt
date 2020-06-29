package com.m3sv.plainupnp.upnp.actions.renderingcontrol

import org.fourthline.cling.model.meta.Service
import javax.inject.Inject

class RaiseVolumeAction @Inject constructor(
    private val setVolumeAction: SetVolumeAction,
    private val getVolumeAction: GetVolumeAction
) {
    suspend operator fun invoke(service: Service<*, *>, step: Int): Int =
        setVolumeAction(service, getVolumeAction(service) + step)
}
