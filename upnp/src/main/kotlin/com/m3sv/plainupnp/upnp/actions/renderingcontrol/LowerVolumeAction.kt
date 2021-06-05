package com.m3sv.plainupnp.upnp.actions.renderingcontrol

import org.fourthline.cling.model.meta.Service
import javax.inject.Inject
import kotlin.math.abs

class LowerVolumeAction @Inject constructor(
    private val setVolumeAction: SetVolumeAction,
    private val getVolumeAction: GetVolumeAction
) {
    suspend operator fun invoke(
        renderingService: Service<*, *>,
        step: Int
    ): Int {
        val currentVolume = getVolumeAction(renderingService)

        var delta = currentVolume - step

        if (delta < 0) {
            delta += abs(delta)
        }

        return setVolumeAction(renderingService, delta)
    }
}
