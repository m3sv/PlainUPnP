package com.m3sv.plainupnp.presentation.main

enum class PlayerButton {
    PLAY,
    PREVIOUS,
    NEXT,
    RAISE_VOLUME,
    LOWER_VOLUME
}

sealed class MainIntention {
    data class PlayerButtonClick(val button: PlayerButton) : MainIntention()
    data class SelectRenderer(val position: Int) : MainIntention()
    data class SelectContentDirectory(val position: Int) : MainIntention()
    data class MoveTo(val progress: Int) : MainIntention()
    data class Filter(val text: String) : MainIntention()
    object ResumeUpnp : MainIntention()
    object PauseUpnp : MainIntention()
}
