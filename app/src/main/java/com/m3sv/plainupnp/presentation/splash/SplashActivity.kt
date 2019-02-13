package com.m3sv.plainupnp.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.m3sv.plainupnp.common.utils.isRunningOnTv
import com.m3sv.plainupnp.presentation.main.MainActivity
import com.m3sv.plainupnp.presentation.tv.TvActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isRunningOnTv())
            startActivity(Intent(this, MainActivity::class.java))
        else
            startActivity(Intent(this, TvActivity::class.java))

        finish()
    }
}