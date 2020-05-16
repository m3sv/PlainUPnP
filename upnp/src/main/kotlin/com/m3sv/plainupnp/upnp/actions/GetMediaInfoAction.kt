package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.RendererServiceFinder
import org.fourthline.cling.UpnpService
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo
import org.fourthline.cling.support.model.MediaInfo
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetMediaInfoAction @Inject constructor(
    upnpService: UpnpService,
    serviceFinder: RendererServiceFinder
) : AvAction(upnpService, serviceFinder) {

    suspend operator fun invoke() =
        suspendCoroutine<MediaInfo?> { continuation ->
            executeAVAction {
                object : GetMediaInfo(this) {
                    override fun received(
                        invocation: ActionInvocation<out Service<*, *>>?,
                        mediaInfo: MediaInfo?
                    ) {
                        continuation.resume(mediaInfo)
                    }

                    override fun failure(
                        p0: ActionInvocation<out Service<*, *>>?,
                        p1: UpnpResponse?,
                        p2: String?
                    ) {
                        Timber.e("Failed to set URI")
                        continuation.resumeWith(Result.failure(IllegalStateException("Failed to get media info")))
                    }
                }
            }
        }

}
