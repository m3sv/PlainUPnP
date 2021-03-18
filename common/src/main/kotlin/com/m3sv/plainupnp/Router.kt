package com.m3sv.plainupnp

import android.content.Context
import android.content.Intent

interface Router {
    fun getNextIntent(context: Context): Intent
}
