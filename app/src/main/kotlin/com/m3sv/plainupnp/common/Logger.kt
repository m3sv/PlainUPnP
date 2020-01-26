package com.m3sv.plainupnp.common

import timber.log.Timber

inline fun Any.log(message: String) {
    Timber.d(message)
}
