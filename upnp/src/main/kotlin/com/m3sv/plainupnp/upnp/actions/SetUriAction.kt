package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.RendererServiceFinder
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import org.fourthline.cling.UpnpService
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SetUriAction @Inject constructor(
    upnpService: UpnpService,
    serviceFinder: RendererServiceFinder
) : AvAction(upnpService, serviceFinder) {

    suspend operator fun invoke(uri: String?, trackMetadata: TrackMetadata) =
        suspendCoroutine<Boolean> { continuation ->
            executeAVAction {
                object : SetAVTransportURI(this, uri, trackMetadata.xml) {

                    override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                        continuation.resume(true)
                    }

                    override fun failure(
                        p0: ActionInvocation<out Service<*, *>>?,
                        p1: UpnpResponse?,
                        p2: String?
                    ) {
                        Timber.e("Failed to set URI")
                        continuation.resume(false)
                    }
                }
            }
        }

}
