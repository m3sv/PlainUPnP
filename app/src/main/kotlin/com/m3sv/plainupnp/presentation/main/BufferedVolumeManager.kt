package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.upnp.volume.UpnpVolumeManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import javax.inject.Inject


class BufferedVolumeManager @Inject constructor(volumeManager: UpnpVolumeManager) :
    UpnpVolumeManager by volumeManager {

    private var timeoutJob: Job? = null

    private var currentStep: Int = 1
        set(value) {
            if (value <= MAX_STEP)
                field = value
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
        timeoutJob = async {
            delay(2000)
            currentStep = 1
        }

        currentStep++
    }

    companion object {
        private const val MAX_STEP = 5
    }
}
