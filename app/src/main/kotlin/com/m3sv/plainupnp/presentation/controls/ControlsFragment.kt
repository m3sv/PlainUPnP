package com.m3sv.plainupnp.presentation.controls

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.ALPHA
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.common.BottomSheetCallback
import com.m3sv.plainupnp.common.OnSlideAction
import com.m3sv.plainupnp.common.OnStateChangedAction
import com.m3sv.plainupnp.common.TriggerOnceStateAction
import com.m3sv.plainupnp.common.utils.hide
import com.m3sv.plainupnp.common.utils.onItemSelectedListener
import com.m3sv.plainupnp.common.utils.onSeekBarChangeListener
import com.m3sv.plainupnp.common.utils.show
import com.m3sv.plainupnp.databinding.ControlsFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import com.m3sv.plainupnp.presentation.main.MainActivity
import timber.log.Timber
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE


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

class ControlsFragment : BaseFragment() {

    @Inject
    lateinit var controlsSheetDelegate: ControlsSheetDelegate

    var actionCallback: ControlsActionCallback? = null

    private lateinit var binding: ControlsFragmentBinding

    private lateinit var rendererAdapter: SimpleArrayAdapter<SpinnerItem>

    private lateinit var contentDirectoriesAdapter: SimpleArrayAdapter<SpinnerItem>

    private val bottomSheetCallback: BottomSheetCallback = BottomSheetCallback()

    private val behavior: BottomSheetBehavior<ConstraintLayout> by lazy(NONE) {
        BottomSheetBehavior.from(binding.backgroundContainer)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).mainActivitySubComponent.inject(this)
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

    private val alphaAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.scrimView, ALPHA, .5f).apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    alphaHideAnimator.cancel()
                    binding.scrimView.show()
                }
            })
        }
    }

    private val alphaHideAnimator: ObjectAnimator by lazy {
        ObjectAnimator.ofFloat(binding.scrimView, ALPHA, 0f).apply {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    binding.scrimView.hide()
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationStart(animation: Animator?) {
                    alphaAnimator.cancel()
                }
            })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rendererAdapter = SimpleArrayAdapter.init(binding.root.context)
        contentDirectoriesAdapter = SimpleArrayAdapter.init(binding.root.context)

        if (savedInstanceState != null) {
            rendererAdapter.onRestoreInstanceState(savedInstanceState)
            contentDirectoriesAdapter.onRestoreInstanceState(savedInstanceState)

            val backPressedCallbackEnabled =
                savedInstanceState.getBoolean(IS_CALLBACK_ENABLED_KEY, false)

            onBackPressedCallback.isEnabled = backPressedCallbackEnabled

            if (backPressedCallbackEnabled) {
                controlsSheetDelegate.onShow()
            } else {
                controlsSheetDelegate.onDismiss()
            }

            binding.scrimView.alpha = savedInstanceState.getFloat(SCRIM_VIEW_ALPHA_KEY, 0f)
        }

        behavior.addBottomSheetCallback(bottomSheetCallback)

        addOnStateChangedAction(TriggerOnceStateAction { isHidden ->
            onBackPressedCallback.isEnabled = !isHidden

            if (isHidden) {
                controlsSheetDelegate.onDismiss()
                alphaHideAnimator.start()
            }
        })

        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        with(binding) {
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

            scrimView.setOnClickListener { close() }

            with(mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener =
                    onItemSelectedListener { position ->
                        Timber.d("Renderer click: $position")
                        actionCallback?.onAction(ControlsAction.SelectRenderer(position - 1))
                    }
            }

            with(mainContentDevicePicker) {
                adapter = contentDirectoriesAdapter
                onItemSelectedListener =
                    onItemSelectedListener { position ->
                        Timber.d("Content directory click: $position")
                        actionCallback?.onAction(ControlsAction.SelectContentDirectory(position - 1))
                    }
            }
        }
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
        controlsSheetDelegate.onShow()
        alphaAnimator.start()
    }

    fun close() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        alphaHideAnimator.start()
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
        rendererAdapter.setNewItems(items.addEmptyItem())
    }

    fun setContentDirectories(items: List<SpinnerItem>) {
        contentDirectoriesAdapter.setNewItems(items.addEmptyItem())
    }

    private fun List<SpinnerItem>.addEmptyItem() =
        this.toMutableList().apply { add(0, SpinnerItem("Empty item")) }.toList()

    override fun onSaveInstanceState(outState: Bundle) {
        rendererAdapter.onSaveInstanceState(outState)
        contentDirectoriesAdapter.onSaveInstanceState(outState)
        outState.putBoolean(IS_CALLBACK_ENABLED_KEY, onBackPressedCallback.isEnabled)

        // careful with lateinit in onSaveInstanceState
        if (this::binding.isInitialized)
            outState.putFloat(SCRIM_VIEW_ALPHA_KEY, binding.scrimView.alpha)
    }

    companion object {
        private const val IS_CALLBACK_ENABLED_KEY = "controls_sheet_expanded_state"
        private const val SCRIM_VIEW_ALPHA_KEY = "scrim_view_alpha_key"
    }
}
