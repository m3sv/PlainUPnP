package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.upnp.RenderedItem

sealed class Route {
    object Back : Route()
    data class To(val path: String) : Route()
}

sealed class MainIntention {
    object ResumeUpnp : MainIntention()
    object PauseUpnp : MainIntention()
    object PlayClick : MainIntention()
    object PreviousClick : MainIntention()
    object NextClick : MainIntention()
    object RaiseVolume : MainIntention()
    object LowerVolume : MainIntention()
    data class Navigate(val route: Route) : MainIntention()
    data class SelectRenderer(val position: Int) : MainIntention()
    data class SelectContentDirectory(val position: Int) : MainIntention()
    data class MoveTo(val progress: Int) : MainIntention()
}

sealed class MainState {
    data class RenderersDiscovered(val devices: List<DeviceDisplay>) : MainState()
    data class ContentDirectoriesDiscovered(val devices: List<DeviceDisplay>) : MainState()
    data class UpdateRendererState(val rendererState: RendererState) : MainState()
    data class RenderItem(val item: RenderedItem) : MainState()
    object NavigateBack : MainState()
    object Exit : MainState()
}