package fi.iki.elonen

import fi.iki.elonen.nanohttpd.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class CoroutineAsyncRunner : NanoHTTPD.AsyncRunner {
    override fun exec(runnable: Runnable) {
        GlobalScope.launch(context = Dispatchers.Default) { runnable.run() }
    }
}