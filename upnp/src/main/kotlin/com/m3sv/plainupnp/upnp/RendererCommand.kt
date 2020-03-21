package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.upnp.didl.ClingDIDLItem
import com.m3sv.plainupnp.upnp.trackmetadata.TrackMetadata
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fourthline.cling.controlpoint.ActionCallback
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
import timber.log.Timber


class RendererCommand(
    private val controller: UpnpServiceController,
    private val controlPoint: ControlPoint,
    private val innerState: UpnpInnerState
) {
    private var job: Job? = null

    private var innerStopCounter = 0

    private var paused: Boolean = true

    fun pause() {
        Timber.v("Pause renderer")
        job?.cancel()
        paused = true
    }

    fun resume() {
        Timber.v("Resume renderer")
        job?.cancel()
        job = GlobalScope.launch { updateInfo() }
        paused = false
    }

    fun commandPlay() = executeAVAction {
        object : Play(it) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success playing")
                // TODO update player state
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Failed to play $arg2")
            }
        }
    }

    fun commandStop() = executeAVAction {
        object : Stop(it) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success stopping ! ")
                // TODO update player state
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to stop ! $arg2")
            }
        }
    }

    fun commandPause() = executeAVAction {
        object : Pause(it) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success pausing ! ")
                // TODO update player state
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to stop ! $arg2")
            }
        }
    }

    fun commandSeek(relativeTimeTarget: String) = executeAVAction {
        Timber.v("Seek to $relativeTimeTarget")
        object : Seek(it, relativeTimeTarget) {

            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success seeking, $invocation")

                // TODO get rid of this
                GlobalScope.launch {
                    delay(1000)
                    updatePositionInfo(true)
                    resume()
                }
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.e("Failure to seek: $arg2")
            }
        }
    }

    fun setMute(mute: Boolean) = executeRenderingAction {
        object : SetMute(it, mute) {
            override fun success(invocation: ActionInvocation<*>?) {
                Timber.v("Success setting mute status ! ")
                innerState.isMute = mute
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to set mute status ! $arg2")
            }
        }
    }

    fun toggleMute() {
        setMute(!innerState.isMute)
    }

    fun setURI(uri: String?, trackMetadata: TrackMetadata) = executeAVAction {
        Timber.i("Set uri to $uri")

        object : SetAVTransportURI(it, uri, trackMetadata.xml) {

            override fun success(invocation: ActionInvocation<*>?) {
                super.success(invocation)
                Timber.i("URI successfully set !")
                commandPlay()
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to set URI ! $arg2")
            }
        }
    }

    fun launchItem(item: DIDLItem) = executeAVAction {
        val obj = (item as ClingDIDLItem).didlObject as? Item ?: return

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
        val trackMetadata = obj.run {
            TrackMetadata(
                id,
                title,
                creator,
                "",
                "",
                firstResource.value,
                "object.item.$type"
            )
        }

        Timber.i("TrackMetadata : $trackMetadata")

        // Stop playback before setting URI

        object : Stop(it) {
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
        }

    }

    private fun updateMediaInfo() = executeAVAction {
        object : GetMediaInfo(it) {
            override fun received(arg0: ActionInvocation<*>, arg1: MediaInfo) {
                innerState.mediaInfo = arg1
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get media info ! $arg2")
            }
        }
    }

    private fun updatePositionInfo(ignorePaused: Boolean = false) = executeAVAction {
        object : GetPositionInfo(it) {
            override fun received(arg0: ActionInvocation<*>, arg1: PositionInfo) {
                if (!paused || ignorePaused)
                    innerState.positionInfo = arg1
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get position info ! $arg2")
            }
        }
    }

    private fun updateTransportInfo() = executeAVAction {
        object : GetTransportInfo(it) {
            override fun received(arg0: ActionInvocation<*>, arg1: TransportInfo) {
                innerState.transportInfo = arg1

                innerStopCounter = when (arg1.currentTransportState) {
                    TransportState.STOPPED -> ++innerStopCounter
                    else -> 0
                }

                checkStopStateThreshold()
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get position info ! $arg2")
            }
        }
    }

    private fun checkStopStateThreshold() {
        if (innerStopCounter == 3) {
            Timber.v("Reached stop threshold, disposing command")
            pause()
        }
    }

    private fun updateVolume() = executeRenderingAction {
        object : GetVolume(it) {
            override fun received(arg0: ActionInvocation<*>, arg1: Int) {
                Timber.d("Receive volume ! $arg1")
                innerState.volume = arg1
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get volume ! $arg2")
            }
        }
    }

    private fun updateMute() = executeRenderingAction {
        object : GetMute(it) {
            override fun received(arg0: ActionInvocation<*>, arg1: Boolean) {
                Timber.d("Receive mute status ! $arg1")
                innerState.isMute = arg1
            }

            override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                Timber.w("Fail to get mute status ! $arg2")
            }
        }
    }


    private fun getRenderingControlService(): Service<*, *>? =
        controller.selectedRenderer?.let {
            (it as CDevice).device?.findService(UDAServiceType("RenderingControl"))
        }

    private fun getAVTransportService(): Service<*, *>? =
        controller.selectedRenderer?.let {
            (it as CDevice).device?.findService(UDAServiceType("AVTransport"))
        }

    private inline fun executeRenderingAction(callback: (Service<*, *>) -> ActionCallback) {
        getRenderingControlService()?.let { service ->
            controlPoint.execute(callback.invoke(service))
        }
    }

    private inline fun executeAVAction(callback: (Service<*, *>) -> ActionCallback) {
        getAVTransportService()?.let { service ->
            controlPoint.execute(callback.invoke(service))
        }
    }


    fun updateFull() {
        updateMediaInfo()
        updatePositionInfo()
        updateVolume()
        updateMute()
        updateTransportInfo()
    }

    private suspend fun updateInfo() {
        var counter = 0

        while (true) {
            if (counter % 6 == 0) {
                updateMediaInfo()
            }

            updatePositionInfo()
            updateTransportInfo()

            delay(1000)
            counter++
        }
    }
}
