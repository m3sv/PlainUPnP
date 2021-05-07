package com.m3sv.plainupnp.presentation.settings.ratehandler

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.m3sv.plainupnp.presentation.settings.R
import timber.log.Timber
import javax.inject.Inject

class OssRateHandler @Inject constructor() : RateHandler {
    override fun rate(activity: Activity) {
        try {
            activity.openPlayStore()
        } catch (e: ActivityNotFoundException) {
            Timber.e("Couldn't launch play store")

            try {
                activity.openPlayStoreFallback()
            } catch (e: ActivityNotFoundException) {
                Timber.e("Couldn't launch play store fallback")
            }
        }
    }

    private fun Activity.openPlayStore() =
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.market_prefix)}$packageName")))

    private fun Activity.openPlayStoreFallback() {
        Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.play_prefix)}$packageName")).also(::startActivity)
    }
}
