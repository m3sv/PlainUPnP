package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.upnp.LocalModel
import com.m3sv.plainupnp.upnp.RenderedItem

sealed class Route {
    object Back : Route()
    data class To(val path: String) : Route()
}

sealed class MainCommand {
    object ResumeUpnp : MainCommand()
    object PauseUpnp : MainCommand()
    object PlayClicked : MainCommand()
    object PreviousClicked : MainCommand()
    object NextClicked : MainCommand()
    data class Navigate(val route: Route) : MainCommand()
    data class SelectRenderer(val position: Int) : MainCommand()
    data class SelectContentDirectory(val position: Int) : MainCommand()
    data class MoveTo(val progress: Int) : MainCommand()
}

sealed class MainState {
    data class LaunchLocally(val model: LocalModel) : MainState()
    data class RenderersDiscovered(val devices: List<DeviceDisplay>) : MainState()
    data class ContentDirectoriesDiscovered(val devices: List<DeviceDisplay>) : MainState()
    data class UpdateRendererState(val rendererState: RendererState) : MainState()
    data class RenderItem(val item: RenderedItem) : MainState()
    object NavigateBack : MainState()
    object Exit : MainState()
}