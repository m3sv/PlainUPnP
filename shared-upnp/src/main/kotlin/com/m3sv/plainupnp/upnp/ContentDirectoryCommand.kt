package com.m3sv.plainupnp.upnp

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
import java.util.concurrent.Future

typealias ContentCallback = (List<ClingDIDLObject>?) -> Unit

class ContentDirectoryCommand(
    private val controlPoint: ControlPoint
) {
    //    private val contentDirectoryService: Service<*, *>?
//        get() = if (controller.selectedContentDirectory == null)
//            null
//        else
//            (controller.selectedContentDirectory as CDevice).device.findService(UDAServiceType("ContentDirectory"))
//
    private fun buildContentList(
        parent: String?,
        didl: DIDLContent
    ): List<ClingDIDLObject> {
        val result = mutableListOf<ClingDIDLObject>()

        if (parent != null) {
            result.add(ClingDIDLParentContainer(parent))
        }

        for (item in didl.containers) {
            result.add(ClingDIDLContainer(item))
            Timber.v("Add container: %s", item.title)
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

    fun browse(
        contentDirectoryService: Service<*, *>?,
        directoryID: String,
        parent: String?,
        callback: ContentCallback
    ): Future<*>? {
        return controlPoint.execute(object : Browse(
            contentDirectoryService,
            directoryID,
            BrowseFlag.DIRECT_CHILDREN,
            "*",
            0,
            null
        ) {
            override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
                callback(buildContentList(parent, didl))
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
                callback(null)
            }
        })
    }
}
