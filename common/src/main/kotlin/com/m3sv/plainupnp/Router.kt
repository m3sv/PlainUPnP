package com.m3sv.plainupnp

import android.content.Context
import android.content.Intent

interface Router {
    fun getMainActivityIntent(context: Context): Intent
    fun getSplashActivityIntent(context: Context): Intent
}
