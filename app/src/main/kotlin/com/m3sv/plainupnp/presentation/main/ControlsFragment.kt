package com.m3sv.plainupnp.presentation.main

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.View.ALPHA
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.BottomSheetCallback
import com.m3sv.plainupnp.common.OnSlideAction
import com.m3sv.plainupnp.common.OnStateChangedAction
import com.m3sv.plainupnp.common.TriggerOnceStateAction
import com.m3sv.plainupnp.common.util.*
import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.ControlsFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import org.fourthline.cling.support.model.TransportState
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class ControlsFragment : BaseFragment() {

    @Inject
    lateinit var controlsSheetDelegate: ControlsSheetDelegate

    private var _binding: ControlsFragmentBinding? = null

    private val binding: ControlsFragmentBinding
        get() = requireNotNull(_binding)

    private lateinit var viewModel: MainViewModel

    private lateinit var rendererAdapter: SimpleArrayAdapter<SpinnerItem>

    private lateinit var contentDirectoriesAdapter: SimpleArrayAdapter<SpinnerItem>

    private val bottomSheetCallback: BottomSheetCallback =
        BottomSheetCallback()

    private val behavior by lazy(NONE) {
        BottomSheetBehavior.from(binding.backgroundContainer)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() = close()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).mainActivitySubComponent.inject(this)
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        viewModel = getViewModel()
        initAdapters()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ControlsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            restorePreviousState(savedInstanceState)
        } else {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        behavior.addBottomSheetCallback(bottomSheetCallback)

        addOnStateChangedAction(TriggerOnceStateAction { isHidden ->
            onBackPressedCallback.isEnabled = !isHidden

            if (isHidden) {
                controlsSheetDelegate.onDismiss()
                alphaHideAnimator.start()
            }
        })

        addOnSlideAction(object : OnSlideAction {
            override fun onSlide(sheet: View, slideOffset: Float) {
                val slideAlpha = 1 - slideOffset

                with(binding.pickers.root) {
                    alpha = slideAlpha
                    visibility = if (slideAlpha != 0f) View.VISIBLE else View.INVISIBLE
                    isClickable = isVisible
                }

                binding.art.alpha = slideOffset
            }
        })

        with(binding) {
            progress.isEnabled = false

            next.setOnClickListener {
                viewModel.playerButtonClick(PlayerButton.NEXT)
            }

            previous.setOnClickListener {
                viewModel.playerButtonClick(PlayerButton.PREVIOUS)
            }

            play.setOnClickListener {
                viewModel.playerButtonClick(PlayerButton.PLAY)
            }

            progress.setOnSeekBarChangeListener(onSeekBarChangeListener { progress ->
                viewModel.moveTo(progress)
            })

            scrimView.setOnClickListener { close() }

            with(pickers.mainContentDevicePicker) {
                setAdapter(contentDirectoriesAdapter)
                setOnItemClickListener { _, _, position, _ ->
                    viewModel.selectContentDirectory(position)
                }
            }

            with(pickers.mainRendererDevicePicker) {
                setAdapter(rendererAdapter)
                setOnItemClickListener { _, _, position, _ ->
                    viewModel.selectRenderer(position)
                }
            }
        }

        viewModel.upnpState.observe(viewLifecycleOwner) { upnpRendererState ->
            handleRendererState(upnpRendererState)
        }

        viewModel.renderers.observe(viewLifecycleOwner) { renderers ->
            setRenderers(renderers)
        }

        viewModel.contentDirectories.observe(viewLifecycleOwner) { contentDirectories ->
            setContentDirectories(contentDirectories)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        saveAdaptersState(outState)
        saveViewState(outState)
        saveBackPressedCallbackState(outState)
    }

    private fun restorePreviousState(savedInstanceState: Bundle) {
        restoreAdaptersState(savedInstanceState)
        restoreBehaviorState(savedInstanceState)
        restoreScrimViewState(savedInstanceState)
        restoreBackPressedCallbackState(savedInstanceState)
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
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        controlsSheetDelegate.onShow()
        binding.scrimView.alpha = 0f
        alphaAnimator.start()
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

    private fun setRenderers(bundle: SpinnerItemsBundle) {
        selectedRendererIndex = bundle.selectedDeviceIndex
        selectedRendererName = bundle.selectedDeviceName
        rendererAdapter.setNewItems(bundle.devices)
    }

    private fun setContentDirectories(bundle: SpinnerItemsBundle) {
        selectedContentDirectoryIndex = bundle.selectedDeviceIndex
        selectedContentDirectoryName = bundle.selectedDeviceName
        contentDirectoriesAdapter.setNewItems(bundle.devices)
    }

    private var selectedContentDirectoryName: String? = null
    private var selectedContentDirectoryIndex: Int = -1

    private var selectedRendererName: String? = null
    private var selectedRendererIndex: Int = -1

    private fun initAdapters() {
        rendererAdapter = SimpleArrayAdapter.init(requireContext(), RENDERERS_ADAPTER_KEY) {
            if (selectedRendererIndex != -1 && selectedRendererName != null) {
                binding.pickers.mainRendererDevicePicker.setText(selectedRendererName)
                viewModel.selectRenderer(selectedRendererIndex)
            }
        }

        contentDirectoriesAdapter = SimpleArrayAdapter.init(requireContext(), CONTENT_ADAPTER_KEY) {
            if (selectedContentDirectoryIndex != -1 && selectedContentDirectoryName != null) {
                binding.pickers.mainContentDevicePicker.setText(selectedContentDirectoryName)
                viewModel.selectContentDirectory(selectedContentDirectoryIndex)
            }
        }
    }

    private fun handleRendererState(rendererState: UpnpRendererState?) {
        if (rendererState == null) return

        val isProgressEnabled = when (rendererState.state) {
            TransportState.PLAYING,
            TransportState.PAUSED_PLAYBACK -> true
            else -> false
        }

        rendererState.elapsedPercent?.let(::setProgress)

        with(binding) {
            progress.isEnabled = isProgressEnabled
            play.setImageResource(rendererState.icon)
            title.text = rendererState.title
            played.text = rendererState.position
            duration.text = rendererState.duration
        }

        when (rendererState.type) {
            UpnpItemType.AUDIO -> setThumbnail(R.drawable.ic_media_placeholder)
            else -> rendererState.uri?.let(::setThumbnail)
        }
    }

    private fun setThumbnail(url: String) {
        Glide.with(this).load(url).into(binding.art)
    }

    private fun setThumbnail(@DrawableRes resource: Int) {
        binding.art.setImageResource(resource)
    }

    private fun setProgress(progress: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.progress.setProgress(progress, true)
        } else {
            binding.progress.progress = progress
        }
    }

    private val alphaAnimator: ObjectAnimator by lazy {
        ObjectAnimator
            .ofFloat(binding.scrimView, ALPHA, .5f)
            .onAnimationStart {
                alphaHideAnimator.cancel()
                binding.scrimView.show()
            }
    }

    private val alphaHideAnimator: ObjectAnimator by lazy {
        ObjectAnimator
            .ofFloat(binding.scrimView, ALPHA, 0f)
            .onAnimationEnd { binding.scrimView.hide() }
            .onAnimationStart { alphaAnimator.cancel() }
    }

    private fun saveViewState(outState: Bundle) {
        if (_binding != null) {
            saveBehaviorState(outState)
            saveScrimViewState(outState)
        }
    }

    private fun saveBehaviorState(outState: Bundle) {
        outState.putParcelable(
            BEHAVIOR_STATE_KEY,
            behavior.onSaveInstanceState(binding.controlsRoot, binding.backgroundContainer)
        )
    }

    private fun saveAdaptersState(outState: Bundle) {
        rendererAdapter.onSaveInstanceState(outState)
        contentDirectoriesAdapter.onSaveInstanceState(outState)
    }

    private fun saveScrimViewState(outState: Bundle) {
        outState.putFloat(SCRIM_VIEW_ALPHA_KEY, binding.scrimView.alpha)
        outState.putInt(SCRIM_VIEW_VISIBILITY_KEY, binding.scrimView.visibility)
    }

    private fun saveBackPressedCallbackState(outState: Bundle) {
        outState.putBoolean(BACK_PRESSED_CALLBACK_KEY, onBackPressedCallback.isEnabled)
    }

    private fun restoreScrimViewState(savedInstanceState: Bundle) {
        val scrimViewAlpha = savedInstanceState.getFloat(SCRIM_VIEW_ALPHA_KEY)
        binding.scrimView.alpha = scrimViewAlpha
        val scrimViewVisibility = savedInstanceState.getInt(SCRIM_VIEW_VISIBILITY_KEY)
        binding.scrimView.visibility = scrimViewVisibility
    }

    private fun restoreBehaviorState(savedInstanceState: Bundle) {
        val behaviorState = savedInstanceState.getParcelable<Parcelable>(BEHAVIOR_STATE_KEY)

        if (behaviorState != null)
            behavior.onRestoreInstanceState(
                binding.controlsRoot,
                binding.backgroundContainer,
                behaviorState
            )
    }

    private fun restoreAdaptersState(savedInstanceState: Bundle) {
        rendererAdapter.onRestoreInstanceState(savedInstanceState)
        contentDirectoriesAdapter.onRestoreInstanceState(savedInstanceState)
    }

    private fun restoreBackPressedCallbackState(savedInstanceState: Bundle) {
        onBackPressedCallback.isEnabled = savedInstanceState.getBoolean(BACK_PRESSED_CALLBACK_KEY)
    }

    companion object {
        private const val RENDERERS_ADAPTER_KEY = "renderers"
        private const val CONTENT_ADAPTER_KEY = "contentDirectories"

        private const val SCRIM_VIEW_VISIBILITY_KEY = "SCRIM_VIEW_VISIBILITY"
        private const val SCRIM_VIEW_ALPHA_KEY = "SCRIM_VIEW_ALPHA"
        private const val BEHAVIOR_STATE_KEY = "BEHAVIOR_STATE"
        private const val BACK_PRESSED_CALLBACK_KEY = "BACK_PRESSED_CALLBACK_KEY"
    }
}

private val UpnpRendererState.icon: Int
    inline get() = when (state) {
        TransportState.PLAYING -> R.drawable.ic_pause
        TransportState.STOPPED,
        TransportState.TRANSITIONING,
        TransportState.PAUSED_PLAYBACK,
        TransportState.PAUSED_RECORDING,
        TransportState.RECORDING,
        TransportState.NO_MEDIA_PRESENT,
        TransportState.CUSTOM -> R.drawable.ic_play_arrow
    }

