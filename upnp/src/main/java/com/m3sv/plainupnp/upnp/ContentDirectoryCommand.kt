package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.upnp.didl.*
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.contentdirectory.callback.Browse
import org.fourthline.cling.support.contentdirectory.callback.Search
import org.fourthline.cling.support.model.BrowseFlag
import org.fourthline.cling.support.model.DIDLContent
import org.fourthline.cling.support.model.item.AudioItem
import org.fourthline.cling.support.model.item.ImageItem
import org.fourthline.cling.support.model.item.VideoItem
import timber.log.Timber
import java.util.concurrent.Future

typealias ContentCallback = (List<DIDLObjectDisplay>?) -> Unit

class ContentDirectoryCommand(
        private val controlPoint: ControlPoint,
        private val controller: UpnpServiceController
) {

    private val mediaReceiverRegistarService: Service<*, *>?
        get() = if (controller.selectedContentDirectory == null)
            null
        else (controller.selectedContentDirectory as CDevice).device?.findService(UDAServiceType("X_MS_MediaReceiverRegistar"))

    private val contentDirectoryService: Service<*, *>?
        get() = if (controller.selectedContentDirectory == null)
            null
        else (controller.selectedContentDirectory as CDevice).device?.findService(UDAServiceType("ContentDirectory"))

    private fun buildContentList(
            parent: String?,
            didl: DIDLContent
    ): List<DIDLObjectDisplay> {
        val result = mutableListOf<DIDLObjectDisplay>()

        parent?.let {
            result.add(DIDLObjectDisplay(ClingDIDLParentContainer(it)))
        }

        for (item in didl.containers) {
            result.add(DIDLObjectDisplay(ClingDIDLContainer(item)))
            Timber.v("Add container: %s", item.title)
        }

        for (item in didl.items) {
            val clingItem: ClingDIDLItem = when (item) {
                is VideoItem -> ClingVideoItem(item)
                is AudioItem -> ClingAudioItem(item)
                is ImageItem -> ClingImageItem(item)
                else -> ClingDIDLItem(item)
            }

            result.add(DIDLObjectDisplay(clingItem))

            Timber.v("Add item: %s", item.title)

            for (p in item.properties)
                Timber.v("%s%s", p.descriptorName + " ", p.toString())
        }

        return result
    }

    fun browse(
            directoryID: String,
            parent: String?,
            callback: ContentCallback
    ): Future<*>? = contentDirectoryService?.let {
        return controlPoint.execute(object : Browse(
                it,
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

    fun search(search: String, parent: String?, callback: ContentCallback) {
        contentDirectoryService?.let {
            controlPoint.execute(object : Search(it, parent, search) {
                override fun received(actionInvocation: ActionInvocation<*>, didl: DIDLContent) {
                    try {
                        callback(buildContentList(parent, didl))
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }

                override fun updateStatus(status: Status) {
                    Timber.v("updateStatus ! ")
                }

                override fun failure(
                        invocation: ActionInvocation<*>,
                        operation: UpnpResponse,
                        defaultMsg: String
                ) {
                    Timber.w("Fail to browse ! $defaultMsg")
                }
            })
        }
    }
}
