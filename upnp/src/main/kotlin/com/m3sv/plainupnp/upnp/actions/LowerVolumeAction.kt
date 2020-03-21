package com.m3sv.plainupnp.upnp.actions

import javax.inject.Inject
import kotlin.math.abs

class LowerVolumeAction @Inject constructor(
    private val setVolumeAction: SetVolumeAction,
    private val getVolumeAction: GetVolumeAction
) {
    suspend operator fun invoke(step: Int): Int {
        val currentVolume = getVolumeAction()

        var delta = currentVolume - step

        if (delta < 0) {
            delta += abs(delta)
        }

        return setVolumeAction(delta)
    }
}
