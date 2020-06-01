package com.m3sv.plainupnp.upnp.actions

import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo
import org.fourthline.cling.support.model.MediaInfo
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GetMediaInfoAction @Inject constructor(controlPoint: ControlPoint) :
    AvAction<Unit, MediaInfo?>(controlPoint) {

    override suspend operator fun invoke(
        service: Service<*, *>,
        vararg arguments: Unit
    ): MediaInfo? =
        suspendCoroutine { continuation ->
            Timber.tag(tag).d("Get media info")
            executeAVAction(object : GetMediaInfo(service) {
                override fun received(
                    invocation: ActionInvocation<out Service<*, *>>?,
                    mediaInfo: MediaInfo?
                ) {
                    Timber.tag(tag).d("Received media info")
                    continuation.resume(mediaInfo)
                }

                override fun failure(
                    p0: ActionInvocation<out Service<*, *>>?,
                    p1: UpnpResponse?,
                    p2: String?
                ) {
                    Timber.tag(tag).e("Failed to get media info")
                    continuation.resume(null)
                }
            })
        }

}
