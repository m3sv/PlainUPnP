package com.m3sv.plainupnp.upnp.actions

import javax.inject.Inject

class RaiseVolumeAction @Inject constructor(
    private val setVolumeAction: SetVolumeAction,
    private val getVolumeAction: GetVolumeAction
) {
    suspend operator fun invoke(step: Int): Int = setVolumeAction(getVolumeAction() + step)
}
