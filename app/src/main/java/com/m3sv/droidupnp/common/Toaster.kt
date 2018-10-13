package com.m3sv.droidupnp.common

import android.app.Application
import android.support.annotation.StringRes
import android.widget.Toast
import javax.inject.Inject

class Toaster @Inject constructor(private val application: Application) : Toastable {

    override fun toast(text: String) {
        Toast.makeText(application, text, Toast.LENGTH_SHORT).show()
    }

    override fun toast(@StringRes text: Int, arguments: Any?) {
        Toast.makeText(application, application.getString(text, arguments), Toast.LENGTH_SHORT)
            .show()
    }

    override fun longToast(text: String) {
        Toast.makeText(application, text, Toast.LENGTH_LONG).show()
    }

    override fun longToast(@StringRes text: Int, arguments: Any?) {
        Toast.makeText(application, application.getString(text, arguments), Toast.LENGTH_LONG)
            .show()
    }
}