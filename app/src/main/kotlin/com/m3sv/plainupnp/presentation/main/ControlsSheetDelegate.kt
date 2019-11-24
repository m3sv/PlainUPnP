package com.m3sv.plainupnp.presentation.main

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.common.StatefulComponent
import com.m3sv.plainupnp.common.utils.onItemSelectedListener
import com.m3sv.plainupnp.databinding.ControlsSheetBinding
import com.m3sv.plainupnp.presentation.base.ContentDirectory
import com.m3sv.plainupnp.presentation.base.Renderer
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import timber.log.Timber

class ControlsSheetDelegate(
    controlsSheetBinding: ControlsSheetBinding,
    intentionHandler: (MainIntention) -> Unit
) : StatefulComponent {

    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var rendererAdapter: SimpleArrayAdapter<Renderer> =
        SimpleArrayAdapter.init(controlsSheetBinding.container.context)

    private var contentDirectoryAdapter: SimpleArrayAdapter<ContentDirectory> =
        SimpleArrayAdapter.init(controlsSheetBinding.container.context)

    private var selectedRendererPosition: Int = 0

    private var selectedContentDirectoryPosition: Int = 0

    init {
        with(controlsSheetBinding) {
            bottomSheetBehavior = BottomSheetBehavior.from(container)
            progress.isEnabled = false

            next.setOnClickListener {
                intentionHandler(MainIntention.PlayerButtonClick(PlayerButton.NEXT))
            }

            previous.setOnClickListener {
                intentionHandler(MainIntention.PlayerButtonClick(PlayerButton.PREVIOUS))
            }

            play.setOnClickListener {
                intentionHandler(MainIntention.PlayerButtonClick(PlayerButton.PLAY))
            }

            progress.setOnSeekBarChangeListener(com.m3sv.plainupnp.common.utils.onSeekBarChangeListener {
                intentionHandler(MainIntention.MoveTo(it))
            })

            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    Timber.d("Renderer click: $position")
                    selectedRendererPosition = position
                    intentionHandler(MainIntention.SelectRenderer(position))
                }
            }

            with(mainContentDevicePicker) {
                adapter = contentDirectoryAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    Timber.d("Content directory click: $position")
                    selectedContentDirectoryPosition = position
                    intentionHandler(MainIntention.SelectContentDirectory(position))
                }
            }
        }
    }

    fun updateContentDirectories(contentDirectories: List<ContentDirectory>) {
        contentDirectoryAdapter.setNewItems(contentDirectories)
    }

    fun updateRenderers(renderers: List<Renderer>) {
        rendererAdapter.setNewItems(renderers)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        rendererAdapter.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        rendererAdapter.onRestoreInstanceState(savedInstanceState)
    }
}
