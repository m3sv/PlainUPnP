package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.upnp.didl.ClingDIDLItem
import kotlinx.coroutines.*
import org.droidupnp.legacy.cling.CDevice
import org.droidupnp.legacy.cling.TrackMetadata
import org.droidupnp.legacy.cling.UpnpRendererState
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
import kotlin.coroutines.CoroutineContext


class RendererCommand(
    private val controller: UpnpServiceController,
    private val controlPoint: ControlPoint,
    private val rendererState: UpnpRendererState
) : CoroutineScope {

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    fun pause() {
        Timber.v("Pause renderer")
        job.cancel()
    }

    fun resume() {
        Timber.v("Resume renderer")
        job.cancel()
        job = Job()
        launch { updateInfo() }
    }

    private fun getRenderingControlService(): Service<*, *>? =
        controller.selectedRenderer?.let {
            (it as CDevice).device?.findService(UDAServiceType("RenderingControl"))
        }

    private fun getAVTransportService(): Service<*, *>? =
        controller.selectedRenderer?.let {
            (it as CDevice).device?.findService(UDAServiceType("AVTransport"))
        }

    fun commandPlay() {
        getAVTransportService()?.let {
            controlPoint.execute(object : Play(it) {
                override fun success(invocation: ActionInvocation<*>?) {
                    Timber.v("Success playing")
                    // TODO update player state
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Failed to play $arg2")
                }
            })
        }
    }

    fun commandStop() {
        getAVTransportService()?.let {
            controlPoint.execute(object : Stop(it) {
                override fun success(invocation: ActionInvocation<*>?) {
                    Timber.v("Success stopping ! ")
                    // TODO update player state
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Fail to stop ! $arg2")
                }
            })
        }
    }

    fun commandPause() {
        getAVTransportService()?.let {
            controlPoint.execute(object : Pause(it) {
                override fun success(invocation: ActionInvocation<*>?) {
                    Timber.v("Success pausing ! ")
                    // TODO update player state
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Fail to pause ! $arg2")
                }
            })
        }
    }
//
//    fun commandToggle() {
//        val state = rendererState.state
//        if (state == com.m3sv.plainupnp.data.upnp.UpnpRendererState.State.PLAY) {
//            commandPause()
//        } else {
//            commandPlay()
//        }
//    }

    fun commandSeek(relativeTimeTarget: String) {
        getAVTransportService()?.let {
            controlPoint.execute(object : Seek(it, relativeTimeTarget) {
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
    }

    fun setVolume(volume: Int) {
        getRenderingControlService()?.let {
            controlPoint.execute(object : SetVolume(it, volume.toLong()) {
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
    }

    fun setMute(mute: Boolean) {
        getRenderingControlService()?.let {
            controlPoint.execute(object : SetMute(it, mute) {
                override fun success(invocation: ActionInvocation<*>?) {
                    Timber.v("Success setting mute status ! ")
                    rendererState.setMuted(mute)
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Fail to set mute status ! $arg2")
                }
            })
        }
    }

    fun toggleMute() {
        setMute(!rendererState.isMute)
    }

    fun setURI(uri: String?, trackMetadata: TrackMetadata) {
        Timber.i("Set uri to $uri")

        getAVTransportService()?.let {
            controlPoint.execute(object :
                SetAVTransportURI(it, uri, trackMetadata.xml) {

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
    }

    fun launchItem(item: DIDLItem) {
        getAVTransportService()?.let {
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

            Timber.i("TrackMetadata : " + trackMetadata.toString())

            // Stop playback before setting URI
            controlPoint.execute(object : Stop(it) {
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
    }

    private fun updateMediaInfo() {
        getAVTransportService()?.let {
            controlPoint.execute(object : GetMediaInfo(it) {
                override fun received(arg0: ActionInvocation<*>, arg1: MediaInfo) {
                    Timber.d("Receive media info ! $arg1")
                    rendererState.setMediaInfo(arg1)
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Fail to get media info ! $arg2")
                }
            })
        }
    }

    private fun updatePositionInfo() {
        getAVTransportService()?.let {
            controlPoint.execute(object : GetPositionInfo(it) {
                override fun received(arg0: ActionInvocation<*>, arg1: PositionInfo) {
                    Timber.d("Update position info: $arg1")
                    rendererState.setPositionInfo(arg1)
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Fail to get position info ! $arg2")
                }
            })
        }
    }

    private var previousTransportState = TransportState.STOPPED

    private fun updateTransportInfo() {
        getAVTransportService()?.let {
            controlPoint.execute(object : GetTransportInfo(it) {
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
    }

    fun updateVolume() {
        getRenderingControlService()?.let {
            controlPoint.execute(object : GetVolume(it) {
                override fun received(arg0: ActionInvocation<*>, arg1: Int) {
                    Timber.d("Receive volume ! $arg1")
                    rendererState.setVolume(arg1)
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Fail to get volume ! $arg2")
                }
            })
        }
    }

    private fun updateMute() {
        getRenderingControlService()?.let {
            controlPoint.execute(object : GetMute(it) {
                override fun received(arg0: ActionInvocation<*>, arg1: Boolean) {
                    Timber.d("Receive mute status ! $arg1")
                    rendererState.setMuted(arg1)
                }

                override fun failure(arg0: ActionInvocation<*>, arg1: UpnpResponse, arg2: String) {
                    Timber.w("Fail to get mute status ! $arg2")
                }
            })
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
            Timber.d("Update state!")

            if (counter % 3 == 0) {
                updateVolume()
                updateMute()
            }

            if (counter % 6 == 0) {
                updateMediaInfo()
            }

            updatePositionInfo()
            updateTransportInfo()

            counter++
            delay(1000)
        }
    }
}