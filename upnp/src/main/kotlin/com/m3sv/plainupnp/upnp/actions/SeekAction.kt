package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.RendererServiceFinder
import org.fourthline.cling.UpnpService
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.support.avtransport.callback.Seek
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SeekAction @Inject constructor(
    upnpService: UpnpService,
    serviceFinder: RendererServiceFinder
) : AvAction(upnpService, serviceFinder) {

    suspend operator fun invoke(relativeTimeTarget: String) =
        suspendCoroutine<Unit> { continuation ->
            executeAVAction {
                object : Seek(this, relativeTimeTarget) {

                    override fun success(invocation: ActionInvocation<*>?) {
                        Timber.v("Success seeking, $invocation")
                        continuation.resume(Unit)
                    }

                    override fun failure(
                        arg0: ActionInvocation<*>,
                        arg1: UpnpResponse,
                        arg2: String
                    ) {
                        Timber.e("Failure to seek: $arg2")
                        continuation.resume(Unit)
                    }
                }
            }
        }

}
