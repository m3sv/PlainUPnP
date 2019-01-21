package com.m3sv.plainupnp.presentation.splash

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.m3sv.plainupnp.presentation.main.MainActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, MainActivity::class.java))

        finish()
    }
}