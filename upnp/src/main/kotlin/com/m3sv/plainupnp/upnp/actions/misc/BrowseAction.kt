package com.m3sv.plainupnp.upnp.actions.misc

import com.m3sv.plainupnp.upnp.actions.Action
import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.didl.MiscItem
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.item.AudioItem
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.VideoItem
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BrowseAction @Inject constructor(controlPoint: ControlPoint) :
    Action<String?, List<ClingDIDLObject>>(controlPoint) {

    fun browse(
        service: Service<*, *>,
        objectId: String,
    ): Flow<List<ClingDIDLObject>> = callbackFlow {
        controlPoint.execute(object : Browse(
            service,
            objectId,
            BrowseFlag.DIRECT_CHILDREN,
            "*",
            0,
            null
        ) {
            override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
                Timber.d("Received browse response")
                sendBlocking(buildContentList(didl))
            }

            override fun updateStatus(status: Status) {
                // TODO Add support for progress
                Timber.d("Update browse status $status")
            }

            override fun failure(
                invocation: ActionInvocation<*>,
                operation: UpnpResponse,
                defaultMsg: String,
            ) {
                error("Failed to browse $objectId! Message: $defaultMsg")
            }
        })
    }

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: String?,
    ): List<ClingDIDLObject> = suspendCoroutine { continuation ->
        controlPoint.execute(object : Browse(
            service,
            arguments[0],
            BrowseFlag.DIRECT_CHILDREN,
            "*",
            0,
            null
        ) {
            override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
                Timber.d("Received browse response")
                continuation.resume(buildContentList(didl))
            }

            override fun updateStatus(status: Status) {
                Timber.d("Update browse status $status")
            }

            override fun failure(
                invocation: ActionInvocation<*>,
                operation: UpnpResponse,
                defaultMsg: String,
            ) {
                Timber.w("Fail to browse! $defaultMsg")
                continuation.resume(listOf())
            }
        })
    }

    private fun buildContentList(didl: DIDLContent): List<ClingDIDLObject> {
        val result = mutableListOf<ClingDIDLObject>()

        for (item in didl.containers) {
            result.add(ClingContainer(item))
        }

        for (item in didl.items) {
            val clingItem: ClingDIDLObject = when (item) {
                is VideoItem -> ClingMedia.Video(item)
                is AudioItem -> ClingMedia.Audio(item)
                is ImageItem -> ClingMedia.Image(item)
                else -> MiscItem(item)
            }

            result.add(clingItem)
        }

        return result
    }
}
