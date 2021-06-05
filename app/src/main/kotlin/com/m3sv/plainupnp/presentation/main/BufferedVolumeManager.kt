package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.upnp.volume.UpnpVolumeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates


class BufferedVolumeManager @Inject constructor(volumeManager: UpnpVolumeManager) :
    UpnpVolumeManager by volumeManager {

    private var timeoutJob: Job? = null

    private var currentStep: Int by Delegates.vetoable(1) { _, _, new ->
        new <= MAX_STEP
    }

    suspend fun lowerVolume() {
        lowerVolume(currentStep)
        triggerStep()
    }

    suspend fun raiseVolume() {
        raiseVolume(currentStep)
        triggerStep()
    }

    private suspend fun triggerStep() = coroutineScope {
        timeoutJob?.cancel()
        timeoutJob = launch {
            delay(2000)
            currentStep = 1
        }

        currentStep++
    }

    companion object {
        private const val MAX_STEP = 3
    }
}
