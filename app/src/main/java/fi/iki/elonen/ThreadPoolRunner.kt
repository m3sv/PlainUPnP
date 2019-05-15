package fi.iki.elonen

import fi.iki.elonen.nanohttpd.NanoHTTPD
import java.util.concurrent.Executors


class ThreadPoolRunner : NanoHTTPD.AsyncRunner {

    private val executor = Executors.newFixedThreadPool(32)

    override fun exec(runnable: Runnable) {
        executor.execute(runnable)
    }
}