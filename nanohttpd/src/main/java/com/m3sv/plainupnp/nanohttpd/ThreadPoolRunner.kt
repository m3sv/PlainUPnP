package com.m3sv.plainupnp.nanohttpd

import java.util.concurrent.Executors


class ThreadPoolRunner : NanoHTTPD.AsyncRunner {

    private val executor = Executors.newFixedThreadPool(32)

    override fun exec(runnable: Runnable) {
        executor.execute(runnable)
    }
}