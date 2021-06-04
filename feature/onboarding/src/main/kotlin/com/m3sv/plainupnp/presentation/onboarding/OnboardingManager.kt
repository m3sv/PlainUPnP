package com.m3sv.plainupnp.presentation.onboarding

import android.app.Activity
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.presentation.onboarding.activity.OnboardingActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OnboardingManager(
    private val preferencesRepository: PreferencesRepository,
    private val onboardingCompletedListener: (Activity) -> Unit,
) {
    val isOnboardingCompleted
        get() = preferencesRepository.preferences.value.finishedOnboarding

    fun completeOnboarding(activity: OnboardingActivity) {
        GlobalScope.launch { preferencesRepository.finishOnboarding() }
        onboardingCompletedListener(activity)
        activity.finish()
    }
}
