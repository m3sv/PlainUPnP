package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.upnp.DIDLItem

interface IRendererCommand {

    // Pause/resume backgroud state update
    fun pause()

    fun resume()

    // Status
    fun commandPlay()

    fun commandStop()

    fun commandPause()

    fun commandToggle()

    fun updateStatus()

    // Position
    fun commandSeek(relativeTimeTarget: String)

    fun updatePosition()

    // Volume
    fun setVolume(volume: Int)

    fun setMute(mute: Boolean)

    fun toggleMute()

    fun updateVolume()

    // URI
    fun launchItem(uri: DIDLItem)

    // Full
    fun updateFull()
}
