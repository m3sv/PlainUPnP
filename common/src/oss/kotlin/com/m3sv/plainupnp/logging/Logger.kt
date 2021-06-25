package com.m3sv.plainupnp.logging

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Logger @Inject constructor() : Log {
    override fun e(e: Throwable, remote: Boolean) {
        Timber.e(e)
    }

    override fun e(text: String, remote: Boolean) {
        Timber.e(text)
    }

    override fun d(text: String) {
        Timber.d(text)
    }
}
