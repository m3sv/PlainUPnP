package com.m3sv.plainupnp.presentation.main

import android.os.Bundle
import com.m3sv.plainupnp.common.StatefulComponent
import com.m3sv.plainupnp.common.utils.onItemSelectedListener
import com.m3sv.plainupnp.common.utils.onSeekBarChangeListener
import com.m3sv.plainupnp.databinding.ControlsFragmentBinding
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import timber.log.Timber


sealed class ControlsAction {
    object NextClick : ControlsAction()
    object PreviousClick : ControlsAction()
    object PlayClick : ControlsAction()
    data class ProgressChange(val progress: Int) : ControlsAction()
    data class SelectRenderer(val position: Int) : ControlsAction()
    data class SelectContentDirectory(val position: Int) : ControlsAction()
}


interface ControlsActionCallback {
    fun onAction(action: ControlsAction)
}

class ControlsSheetDelegate(
    controlsSheetBinding: ControlsFragmentBinding
) : StatefulComponent {

    private var rendererAdapter: SimpleArrayAdapter<SpinnerItem> =
        SimpleArrayAdapter.init(controlsSheetBinding.root.context)

    private var spinnerItemAdapter: SimpleArrayAdapter<SpinnerItem> =
        SimpleArrayAdapter.init(controlsSheetBinding.root.context)

    var actionCallback: ControlsActionCallback? = null

    init {
        with(controlsSheetBinding) {
            progress.isEnabled = false

            next.setOnClickListener {
                actionCallback?.onAction(ControlsAction.NextClick)
            }

            previous.setOnClickListener {
                actionCallback?.onAction(ControlsAction.PreviousClick)
            }

            play.setOnClickListener {
                actionCallback?.onAction(ControlsAction.PlayClick)
            }

            progress.setOnSeekBarChangeListener(onSeekBarChangeListener { progress ->
                actionCallback?.onAction(ControlsAction.ProgressChange(progress))
            })

            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    Timber.d("Renderer click: $position")
                    actionCallback?.onAction(ControlsAction.SelectRenderer(position - 1))
                }
            }

            with(mainContentDevicePicker) {
                adapter = spinnerItemAdapter
                onItemSelectedListener = onItemSelectedListener { position ->
                    Timber.d("Content directory click: $position")
                    actionCallback?.onAction(ControlsAction.SelectContentDirectory(position - 1))
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
