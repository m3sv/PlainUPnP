package com.m3sv.plainupnp.nanohttpd

import java.util.concurrent.Executors


class ThreadPoolRunner : AsyncRunner {

    private val executor = Executors.newFixedThreadPool(64)

    override fun exec(runnable: Runnable) {
        executor.execute(runnable)
    }
}
