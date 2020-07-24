package com.m3sv.plainupnp.upnp.actions

import com.m3sv.plainupnp.upnp.didl.*
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

    override suspend fun invoke(
        service: Service<*, *>,
        vararg arguments: String?
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
                continuation.resume(buildContentList(didl))
            }

            override fun updateStatus(status: Status) {
                Timber.v("Update browse status!")
            }

            override fun failure(
                invocation: ActionInvocation<*>,
                operation: UpnpResponse,
                defaultMsg: String
            ) {
                Timber.w("Fail to browse! $defaultMsg")
                continuation.resume(listOf())
            }
        })
    }

    private fun buildContentList(didl: DIDLContent): List<ClingDIDLObject> {
        val result = mutableListOf<ClingDIDLObject>()

        for (item in didl.containers) {
            result.add(ClingDIDLContainer(item))
        }

        for (item in didl.items) {
            val clingItem: ClingDIDLObject = when (item) {
                is VideoItem -> ClingVideoItem(item)
                is AudioItem -> ClingAudioItem(item)
                is ImageItem -> ClingImageItem(item)
                else -> ClingDIDLObject(item)
            }

            result.add(clingItem)
        }

        return result
    }
}
