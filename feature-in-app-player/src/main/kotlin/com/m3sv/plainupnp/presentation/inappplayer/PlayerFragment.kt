package com.m3sv.plainupnp.presentation.inappplayer

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.m3sv.plainupnp.presentation.inappplayer.databinding.PlayerFragmentBinding
import kotlin.LazyThreadSafetyMode.NONE

class PlayerFragment : Fragment(R.layout.player_fragment) {

    private val binding by lazy(NONE) { PlayerFragmentBinding.bind(requireView()) }

    private var player: Player? = null
    private var playWhenReady: Boolean = true
    private var playbackPosition: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            playWhenReady = savedInstanceState.getBoolean(PLAY_WHEN_READY)
            playbackPosition = savedInstanceState.getLong(PLAYBACK_POSITION)
        }
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.N) && (player != null)) {
            initializePlayer();
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            releasePlayer()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        with(outState) {
            putBoolean(PLAY_WHEN_READY, playWhenReady)
            putLong(PLAYBACK_POSITION, playbackPosition)
        }
    }

    private fun initializePlayer() {
        val videoUrl = requireNotNull(requireArguments().getString(URL))

        player = SimpleExoPlayer
            .Builder(requireContext())
            .build()
            .also { player ->
                binding.playerView.player = player
                player.playWhenReady = playWhenReady
                player.setMediaItem(MediaItem.fromUri(videoUrl), playbackPosition)
                player.prepare()
            }
    }

    private fun releasePlayer() {
        player?.let { player ->
            playWhenReady = player.playWhenReady
            playbackPosition = player.currentPosition
            player.release()
        }

        player = null
    }

    private fun hideSystemUi() {
        binding.playerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    companion object {
        private const val PLAY_WHEN_READY = "play_when_ready"
        private const val PLAYBACK_POSITION = "playback_position"

        private const val URL = "resource_url"

        fun newInstance(videoUrl: String): PlayerFragment = PlayerFragment().apply {
            arguments = bundleOf(URL to videoUrl)
        }
    }
}
