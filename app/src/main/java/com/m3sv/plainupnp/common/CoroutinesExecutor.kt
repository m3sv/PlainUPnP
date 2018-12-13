@file:JvmName("CoroutinesExecutor")
package com.m3sv.plainupnp.common

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun launchInGlobalScope(runnable: Runnable) {
    GlobalScope.launch { runnable.run() }
}