package com.m3sv.plainupnp.presentation.main

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.common.StatefulComponent
import com.m3sv.plainupnp.common.utils.onItemSelectedListener
import com.m3sv.plainupnp.databinding.ControlsSheetBinding
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import timber.log.Timber

class ControlsSheetDelegate(
    controlsSheetBinding: ControlsSheetBinding,
    intentionHandler: (MainIntention) -> Unit
) : StatefulComponent {

    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var rendererAdapter: SimpleArrayAdapter<SpinnerItem> =
        SimpleArrayAdapter.init(controlsSheetBinding.container.context)

    private var spinnerItemAdapter: SimpleArrayAdapter<SpinnerItem> =
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
                    intentionHandler(MainIntention.SelectRenderer(position - 1))
                }
            }

            with(mainContentDevicePicker) {
                adapter = spinnerItemAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    Timber.d("Content directory click: $position")
                    selectedContentDirectoryPosition = position
                    intentionHandler(MainIntention.SelectContentDirectory(position - 1))
                }
            }
        }
    }

    fun updateContentDirectories(spinnerItems: List<SpinnerItem>) {
        spinnerItemAdapter.setNewItems(spinnerItems.addEmptyItem())
    }

    fun updateRenderers(renderers: List<SpinnerItem>) {
        rendererAdapter.setNewItems(renderers.addEmptyItem())
    }

    private fun List<SpinnerItem>.addEmptyItem() =
        this.toMutableList().apply { add(0, SpinnerItem("Empty item")) }.toList()

    override fun onSaveInstanceState(outState: Bundle) {
        rendererAdapter.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        rendererAdapter.onRestoreInstanceState(savedInstanceState)
    }
}
