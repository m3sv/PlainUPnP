package com.m3sv.plainupnp.presentation.onboarding

import android.app.Activity
import android.content.SharedPreferences

class OnboardingManager(
    val preferences: SharedPreferences,
    val onboardingCompletedListener: (Activity) -> Unit,
) {

    val isOnboardingCompleted
        get() = preferences.getBoolean(FINISHED_ONBOARDING_KEY, false)

    fun completeOnboarding(activity: OnboardingActivity) {
        preferences.edit().putBoolean(FINISHED_ONBOARDING_KEY, true).apply()
        onboardingCompletedListener(activity)
        activity.finish()
    }

    companion object {
        private const val FINISHED_ONBOARDING_KEY = "finished_onboarding"
    }
}
