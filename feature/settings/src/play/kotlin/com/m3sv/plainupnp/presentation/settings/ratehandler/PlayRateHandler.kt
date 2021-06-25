package com.m3sv.plainupnp.presentation.settings.ratehandler

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.m3sv.plainupnp.logging.Log
import timber.log.Timber
import javax.inject.Inject

class PlayRateHandler @Inject constructor(
    application: Application,
    private val log: Log
) : RateHandler {
    private val manager = ReviewManagerFactory.create(application)

    override fun rate(activity: Activity) {
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener { reviewTask ->
                    if (reviewTask.isSuccessful) {
                        Timber.d("Yay!")
                    } else {
                        reviewTask.logException("Review task was unsuccessful")
                        rateManually(activity)
                    }
                }
            } else {
                task.logException("Failed to in-app rate")
                rateManually(activity)
            }
        }
    }

    private fun Task<*>.logException(message: String) {
        exception?.let { e -> log.e(e, message) } ?: let { log.e(message) }
    }

    private fun rateManually(activity: Activity) {
        try {
            activity.openPlayStore()
        } catch (e: ActivityNotFoundException) {
            log.e("Couldn't launch play store")

            try {
                activity.openPlayStoreFallback()
            } catch (e: ActivityNotFoundException) {
                log.e("Couldn't launch play store fallback")
            }
        }
    }
}
