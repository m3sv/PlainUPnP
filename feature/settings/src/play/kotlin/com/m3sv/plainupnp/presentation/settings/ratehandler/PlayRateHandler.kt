package com.m3sv.plainupnp.presentation.settings.ratehandler

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber
import javax.inject.Inject

class PlayRateHandler @Inject constructor(application: Application) : RateHandler {
    private val manager = ReviewManagerFactory.create(application)

    override fun rate(activity: Activity) {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Timber.d("Successful requestReviewFlow!")
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { reviewTask ->
                    if (reviewTask.isSuccessful) {
                        Timber.d("Yay!")
                    } else {
                        Timber.e(reviewTask.exception)
                        rateManually(activity)
                    }
                }
            } else {
                Timber.e(task.exception)
                rateManually(activity)
            }
        }
    }

    private fun rateManually(activity: Activity) {
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
}
