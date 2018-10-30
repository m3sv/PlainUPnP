package com.m3sv.plainupnp.upnp

import kotlinx.coroutines.experimental.*
import org.droidupnp.legacy.cling.CDevice
import org.droidupnp.legacy.cling.TrackMetadata
import org.droidupnp.legacy.cling.UpnpRendererState
import org.droidupnp.legacy.cling.didl.ClingDIDLItem
import org.droidupnp.legacy.upnp.IRendererCommand
import org.droidupnp.legacy.upnp.didl.IDIDLItem
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.UDAServiceType
import org.fourthline.cling.support.avtransport.callback.*
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.model.TransportState
import org.fourthline.cling.support.model.item.*
import org.fourthline.cling.support.renderingcontrol.callback.GetMute
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume
import org.fourthline.cling.support.renderingcontrol.callback.SetMute
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume
import timber.log.Timber
import kotlin.coroutines.experimental.CoroutineContext


class RendererCommand(
    private val controller: UpnpServiceController,
    private val controlPoint: ControlPoint,
    private val rendererState: UpnpRendererState
) : IRendererCommand, CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun pause() {
        Timber.v("Pause renderer")
        job.cancel()
    }

    override fun resume() {
        Timber.v("Resume renderer")
        job.cancel()
        job = Job()
        launch {
            updateInfo()
        }
    }

    fun getRenderingControlService(): Service<*, *>? {
        return if (controller.selectedRenderer == null) null else (controller.selectedRenderer as CDevice).device.findService(
            UDAServiceType("RenderingControl")
        )
    }

    fun getAVTransportService(): Service<*, *>? {
        return if (controller.selectedRenderer == null) null else (controller.selectedRenderer as CDevice).device.findService(
            UDAServiceType("AVTransport")
        )
    }

    override fun commandPlay() {
        if (getAVTransportService() == null)
            return

        controlPoint.execute(object : Play(getAVTransportService()) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success playing ! ")
                // TODO update player state
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to play ! $arg2")
            }
        })
    }

    override fun commandStop() {
        if (getAVTransportService() == null)
            return

        controlPoint.execute(object : Stop(getAVTransportService()) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success stopping ! ")
                // TODO update player state
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to stop ! $arg2")
            }
        })
    }

    override fun commandPause() {
        if (getAVTransportService() == null)
            return

        controlPoint.execute(object : Pause(getAVTransportService()!!) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success pausing ! ")
                // TODO update player state
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to pause ! $arg2")
            }
        })
    }

    override fun commandToggle() {
        val state = rendererState.state
        if (state == com.m3sv.plainupnp.data.UpnpRendererState.State.PLAY) {
            commandPause()
        } else {
            commandPlay()
        }
    }

    override fun commandSeek(relativeTimeTarget: String) {
        if (getAVTransportService() == null)
            return

        controlPoint.execute(object : Seek(getAVTransportService()!!, relativeTimeTarget) {
            // TODO fix it, what is relativeTimeTarget ? :)

            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success seeking !")
                // TODO update player state
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to seek ! $arg2")
            }
        })
    }

    override fun setVolume(volume: Int) {
        if (getRenderingControlService() == null)
            return

        controlPoint.execute(object : SetVolume(getRenderingControlService()!!, volume.toLong()) {
            override fun success(invocation: ActionInvocation<*>?) {
                super.success(invocation)
                Timber.v("Success to set volume")
                rendererState.setVolume(volume)
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to set volume ! $arg2")
            }
        })
    }

    override fun setMute(mute: Boolean) {
        if (getRenderingControlService() == null)
            return

        controlPoint.execute(object : SetMute(getRenderingControlService()!!, mute) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success setting mute status ! ")
                rendererState.setMuted(mute)
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to set mute status ! $arg2")
            }
        })
    }

    override fun toggleMute() {
        setMute(!rendererState.isMute)
    }

    fun setURI(uri: String?, trackMetadata: TrackMetadata) {
        Timber.i("Set uri to $uri")

        controlPoint.execute(object :
            SetAVTransportURI(getAVTransportService()!!, uri, trackMetadata.xml) {

            override fun success(invocation: ActionInvocation<*>?) {
                super.success(invocation)
                Timber.i("URI successfully set !")
                commandPlay()
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to set URI ! $arg2")
            }
        })
    }

    override fun launchItem(item: IDIDLItem) {
        if (getAVTransportService() == null)
            return

        val obj = (item as ClingDIDLItem).getObject() as? Item ?: return

        var type = ""
        when (obj) {
            is AudioItem -> type = "audioItem"
            is VideoItem -> type = "videoItem"
            is ImageItem -> type = "imageItem"
            is PlaylistItem -> type = "playlistItem"
            is TextItem -> type = "textItem"

            // TODO genre && artURI

            // Stop playback before setting URI
        }

        // TODO genre && artURI
        val trackMetadata = TrackMetadata(
            obj.id, obj.title,
            obj.creator, "", "", obj.firstResource.value,
            "object.item.$type"
        )

        Timber.i("TrackMetadata : " + trackMetadata.toString())

        // Stop playback before setting URI
        controlPoint.execute(object : Stop(getAVTransportService()!!) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success stopping ! ")
                callback()
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to stop ! $arg2")
                callback()
            }

            fun callback() {
                setURI(item.uri, trackMetadata)
            }
        })

    }

    // Update

    private fun updateMediaInfo() {
        if (getAVTransportService() == null)
            return

        controlPoint.execute(object : GetMediaInfo(getAVTransportService()!!) {
            override fun received(arg0: ActionInvocation<*>, arg1: MediaInfo) {
                Timber.d("Receive media info ! $arg1")
                rendererState.setMediaInfo(arg1)
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get media info ! $arg2")
            }
        })
    }

    private fun updatePositionInfo() {
        if (getAVTransportService() == null)
            return

        controlPoint.execute(object : GetPositionInfo(getAVTransportService()) {
            override fun received(arg0: ActionInvocation<*>, arg1: PositionInfo) {
                Timber.d("Update position info: $arg1")
                rendererState.setPositionInfo(arg1)
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get position info ! $arg2")
            }
        })
    }

    private var previousTransportState = TransportState.STOPPED

    private fun updateTransportInfo() {
        if (getAVTransportService() == null)
            return

        controlPoint.execute(object : GetTransportInfo(getAVTransportService()) {
            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get position info ! $arg2")
            }

            override fun received(arg0: ActionInvocation<*>, arg1: TransportInfo) {
                Timber.d("Transport info: $arg1")
                rendererState.setTransportInfo(arg1)
                previousTransportState = arg1.currentTransportState
            }
        })
    }

    override fun updateVolume() {
        if (getRenderingControlService() == null)
            return

        controlPoint.execute(object : GetVolume(getRenderingControlService()) {
            override fun received(arg0: ActionInvocation<*>, arg1: Int) {
                Timber.d("Receive volume ! $arg1")
                rendererState.setVolume(arg1)
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get volume ! $arg2")
            }
        })
    }

    private fun updateMute() {
        if (getRenderingControlService() == null)
            return

        controlPoint.execute(object : GetMute(getRenderingControlService()!!) {
            override fun received(arg0: ActionInvocation<*>, arg1: Boolean) {
                Timber.d("Receive mute status ! $arg1")
                rendererState.setMuted(arg1)
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get mute status ! $arg2")
            }
        })
    }

    override fun updateFull() {
        updateMediaInfo()
        updatePositionInfo()
        updateVolume()
        updateMute()
        updateTransportInfo()
    }

    private suspend fun updateInfo() {
        while (true) {
            Timber.d("Update state!")
            updatePositionInfo()
            updateVolume()
            updateMute()
            updateTransportInfo()
            updateMediaInfo()

            delay(1000)
        }
    }

    override fun updateStatus() {
        // TODO Auto-generated method stub
    }

    override fun updatePosition() {
        // TODO Auto-generated method stub
    }
}