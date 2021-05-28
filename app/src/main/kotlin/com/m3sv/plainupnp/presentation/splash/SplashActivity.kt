package com.m3sv.plainupnp.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.m3sv.plainupnp.presentation.onboarding.OnboardingManager
import com.m3sv.plainupnp.presentation.onboarding.activity.OnboardingActivity
import com.m3sv.selectcontentdirectory.SelectContentDirectoryActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    @Inject
    lateinit var onboardingManager: OnboardingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onboardingManager.isOnboardingCompleted) {
            startActivity(Intent(this, SelectContentDirectoryActivity::class.java))
        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        finish()
    }

    override fun onBackPressed() {
        finishAndRemoveTask()
    }
}
