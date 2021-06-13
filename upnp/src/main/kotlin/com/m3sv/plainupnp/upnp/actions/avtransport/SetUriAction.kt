package com.m3sv.plainupnp.upnp.actions.avtransport

import com.m3sv.plainupnp.upnp.actions.Action
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SetUriAction @Inject constructor(controlPoint: ControlPoint) :
    Action<String, Boolean>(controlPoint) {

    fun setUri(
        service: Service<*, *>,
        uri: String,
        trackMetadata: TrackMetadata,
    ): Flow<Unit> = callbackFlow {
        val tag = "AV"
        Timber.tag(tag).d("Set uri: $uri")
        val action = object : SetAVTransportURI(service, uri, trackMetadata.xml) {

            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                Timber.tag(tag).d("Set uri: $uri success")
                trySendBlocking(Unit)
                close()
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?,
            ) {
                error("Set uri $uri failed")
            }
        }
        controlPoint.execute(action)

        awaitClose()
    }

    suspend operator fun invoke(
        service: Service<*, *>,
        uri: String,
        trackMetadata: TrackMetadata,
    ): Boolean = invoke(service, uri, trackMetadata.xml)

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: String,
    ): Boolean = suspendCoroutine { continuation ->
        val tag = "AV"
        Timber.tag(tag).d("Set uri: ${arguments[0]}")
        val action = object : SetAVTransportURI(service, arguments[0], arguments[1]) {

            override fun success(invocation: ActionInvocation<out Service<*, *>>?) {
                Timber.tag(tag).d("Set uri: ${arguments[0]} success")
                continuation.resume(true)
            }

            override fun failure(
                p0: ActionInvocation<out Service<*, *>>?,
                p1: UpnpResponse?,
                p2: String?,
            ) {
                Timber.tag(tag).e("Failed to set uri: ${arguments[0]}")
                continuation.resume(false)
            }
        }
        controlPoint.execute(action)
    }
}
