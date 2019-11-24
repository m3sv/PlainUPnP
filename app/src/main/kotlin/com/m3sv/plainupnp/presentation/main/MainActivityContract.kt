package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.base.ContentDirectory
import com.m3sv.plainupnp.presentation.base.Renderer

sealed class Route {
    object Back : Route()
    data class To(val path: String) : Route()
}

enum class PlayerButton {
    PLAY,
    PREVIOUS,
    NEXT,
    RAISE_VOLUME,
    LOWER_VOLUME
}

sealed class MainIntention {
    data class PlayerButtonClick(val button: PlayerButton) : MainIntention()
    data class Navigate(val route: Route) : MainIntention()
    data class SelectRenderer(val position: Int) : MainIntention()
    data class SelectContentDirectory(val position: Int) : MainIntention()
    data class MoveTo(val progress: Int) : MainIntention()
    object ResumeUpnp : MainIntention()
    object PauseUpnp : MainIntention()
    object StartUpnpService : MainIntention()
    object StopUpnpService : MainIntention()
}

sealed class MainState {
    data class Render(
        val renderers: List<Renderer> = listOf(),
        val contentDirectories: List<ContentDirectory> = listOf(),
        val rendererState: UpnpRendererState? = null
    ) : MainState()

    object Exit : MainState()
}
