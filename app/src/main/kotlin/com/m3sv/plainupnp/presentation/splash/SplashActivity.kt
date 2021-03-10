package com.m3sv.plainupnp.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.m3sv.plainupnp.presentation.main.MainActivity
import com.m3sv.plainupnp.presentation.onboarding.OnboardingActivity
import com.m3sv.plainupnp.presentation.onboarding.OnboardingManager
import com.m3sv.selectcontentdirectory.SelectContentDirectoryActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    @Inject
    lateinit var onboardingManager: OnboardingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (onboardingManager.isOnboardingCompleted) {
            startActivity(Intent(this, SelectContentDirectoryActivity::class.java).also { intent ->
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                intent.putExtra(SelectContentDirectoryActivity.NEXT_ACTIVITY_INTENT, mainActivityIntent)
            })
        } else {
            startActivity(Intent(this, OnboardingActivity::class.java))
        }

        finish()
    }

    override fun onBackPressed() {
        finishAndRemoveTask()
    }
}
