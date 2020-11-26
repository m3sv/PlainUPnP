package com.m3sv.plainupnp.upnp.playback

interface PlaybackManager {
    fun resumePlayback()
    fun pausePlayback()
    fun togglePlayback()
    fun stopPlayback()
    fun playNext()
    fun playPrevious()
}
