package com.m3sv.plainupnp.presentation.controls

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.common.BottomSheetCallback
import com.m3sv.plainupnp.common.OnSlideAction
import com.m3sv.plainupnp.common.OnStateChangedAction
import com.m3sv.plainupnp.databinding.ControlsFragmentBinding
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import com.m3sv.plainupnp.presentation.main.ControlsActionCallback
import com.m3sv.plainupnp.presentation.main.ControlsSheetDelegate
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE


class ControlsFragment : Fragment() {

    private lateinit var binding: ControlsFragmentBinding

    private val bottomSheetCallback: BottomSheetCallback = BottomSheetCallback()

    private val behavior: BottomSheetBehavior<ConstraintLayout> by lazy(NONE) {
        BottomSheetBehavior.from(binding.backgroundContainer)
    }

    private lateinit var controlsSheetDelegate: ControlsSheetDelegate

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ControlsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        controlsSheetDelegate = ControlsSheetDelegate(binding)

        behavior.addBottomSheetCallback(bottomSheetCallback)

        addOnStateChangedAction(object : OnStateChangedAction {
            override fun onStateChanged(sheet: View, newState: Int) {
                onBackPressedCallback.isEnabled = newState != BottomSheetBehavior.STATE_HIDDEN

                Timber.d("Enable callback: ${onBackPressedCallback.isEnabled}")
            }
        })

        behavior.state = BottomSheetBehavior.STATE_HIDDEN

    }

    fun toggle() {
        when (behavior.state) {
            BottomSheetBehavior.STATE_HIDDEN -> open()

            BottomSheetBehavior.STATE_HALF_EXPANDED,
            BottomSheetBehavior.STATE_EXPANDED,
            BottomSheetBehavior.STATE_COLLAPSED -> close()
        }
    }

    fun open() {
        behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    fun close() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun addOnStateChangedAction(action: OnStateChangedAction) {
        bottomSheetCallback.addOnStateChangedAction(action)
    }

    fun addOnSlideAction(action: OnSlideAction) {
        bottomSheetCallback.addOnSlideAction(action)
    }

    fun setProgress(progress: Int, isEnabled: Boolean) {

    }

    fun setRenderers(items: List<SpinnerItem>) {
        controlsSheetDelegate.updateRenderers(items)
    }

    fun setContentDirectories(items: List<SpinnerItem>) {
        controlsSheetDelegate.updateContentDirectories(items)
    }

    fun setControlsActionCallback(actionCallback: ControlsActionCallback) {
        controlsSheetDelegate.actionCallback = actionCallback
    }

}
