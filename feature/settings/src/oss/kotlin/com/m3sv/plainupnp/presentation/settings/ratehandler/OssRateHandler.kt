package com.m3sv.plainupnp.presentation.settings.ratehandler

import android.app.Activity
import android.content.ActivityNotFoundException
import com.m3sv.plainupnp.logging.Log
import timber.log.Timber
import javax.inject.Inject

class OssRateHandler @Inject constructor(private val log: Log) : RateHandler {
    override fun rate(activity: Activity) {
        try {
            activity.openPlayStore()
        } catch (e: ActivityNotFoundException) {
            Timber.e("Couldn't launch play store")

            try {
                activity.openPlayStoreFallback()
            } catch (e: ActivityNotFoundException) {
                log.e("Couldn't launch play store fallback")
            }
        }
    }
}
