package com.m3sv.plainupnp.presentation.main

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
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
import com.m3sv.plainupnp.common.utils.*
import com.m3sv.plainupnp.data.upnp.UpnpItemType
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.databinding.ControlsFragmentBinding
import com.m3sv.plainupnp.presentation.base.BaseFragment
import com.m3sv.plainupnp.presentation.base.ControlsSheetDelegate
import com.m3sv.plainupnp.presentation.base.SimpleArrayAdapter
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import timber.log.Timber
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class ControlsFragment : BaseFragment() {

    @Inject
    lateinit var controlsSheetDelegate: ControlsSheetDelegate

    private lateinit var binding: ControlsFragmentBinding

    private lateinit var viewModel: MainViewModel

    private lateinit var rendererAdapter: SimpleArrayAdapter<SpinnerItem>

    private lateinit var contentDirectoriesAdapter: SimpleArrayAdapter<SpinnerItem>

    private val bottomSheetCallback: BottomSheetCallback = BottomSheetCallback()

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
        rendererAdapter = SimpleArrayAdapter.init(view.context)
        contentDirectoriesAdapter = SimpleArrayAdapter.init(view.context)

        if (savedInstanceState != null) {
            restorePreviousState(savedInstanceState)
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
                viewModel.intention(MainIntention.PlayerButtonClick(PlayerButton.NEXT))
            }

            previous.setOnClickListener {
                viewModel.intention(MainIntention.PlayerButtonClick(PlayerButton.PREVIOUS))
            }

            play.setOnClickListener {
                viewModel.intention(MainIntention.PlayerButtonClick(PlayerButton.PLAY))
            }

            progress.setOnSeekBarChangeListener(onSeekBarChangeListener { progress ->
                viewModel.intention(MainIntention.MoveTo(progress))
            })

            scrimView.setOnClickListener { close() }

            with(pickers.mainRendererDevicePicker) {
                adapter = rendererAdapter
                onItemSelectedListener =
                    onItemSelectedListener { position ->
                        Timber.d("Renderer click: $position")
                        viewModel.intention(MainIntention.SelectRenderer(position - 1))
                    }
            }

            with(pickers.mainContentDevicePicker) {
                adapter = contentDirectoriesAdapter
                onItemSelectedListener =
                    onItemSelectedListener { position ->
                        Timber.d("Content directory click: $position")
                        viewModel.intention(MainIntention.SelectContentDirectory(position - 1))
                    }
            }
        }

        viewModel.upnpState().observe(viewLifecycleOwner) { upnpRendererState ->
            handleRendererState(upnpRendererState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        rendererAdapter.onSaveInstanceState(outState)
        contentDirectoriesAdapter.onSaveInstanceState(outState)
        controlsSheetDelegate.onDismiss()
    }

    private fun restorePreviousState(savedInstanceState: Bundle) {
        rendererAdapter.onRestoreInstanceState(savedInstanceState)
        contentDirectoriesAdapter.onRestoreInstanceState(savedInstanceState)
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

    fun setRenderers(items: List<SpinnerItem>) {
        rendererAdapter.setNewItems(items.addEmptyItem())
    }

    fun setContentDirectories(items: List<SpinnerItem>) {
        contentDirectoriesAdapter.setNewItems(items.addEmptyItem())
    }

    private fun handleRendererState(rendererState: UpnpRendererState?) {
        if (rendererState == null) return

        val isProgressEnabled = when (rendererState.state) {
            UpnpRendererState.State.PLAY, UpnpRendererState.State.PAUSE -> true
            else -> false
        }

        setProgress(rendererState.elapsedPercent, isProgressEnabled)
        setPlayIcon(rendererState.icon)
        setTitle(rendererState.title)

        when (rendererState.type) {
            UpnpItemType.AUDIO -> setThumbnail(R.drawable.ic_media_placeholder)
            else -> rendererState.uri?.let(::setThumbnail)
        }
    }

    private fun setPlayIcon(@DrawableRes icon: Int) {
        binding.play.setImageResource(icon)
    }

    private fun setTitle(text: String) {
        binding.title.text = text
    }

    private fun setThumbnail(url: String) {
        Glide.with(this).load(url).into(binding.art)
    }

    private fun setThumbnail(@DrawableRes resource: Int) {
        binding.art.setImageResource(resource)
    }

    private fun setProgress(progress: Int, isEnabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.progress.setProgress(progress, true)
        } else {
            binding.progress.progress = progress
        }

        binding.progress.isEnabled = isEnabled
    }

    private val alphaAnimator: ObjectAnimator by lazy {
        binding.scrimView.alpha = 0f
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

    private fun List<SpinnerItem>.addEmptyItem() =
        this.toMutableList().apply { add(0, SpinnerItem("Empty item")) }.toList()
}

private val UpnpRendererState.icon: Int
    inline get() = when (state) {
        UpnpRendererState.State.STOP -> R.drawable.ic_play_arrow
        UpnpRendererState.State.PLAY -> R.drawable.ic_pause
        UpnpRendererState.State.PAUSE -> R.drawable.ic_play_arrow
        UpnpRendererState.State.INITIALIZING -> R.drawable.ic_play_arrow
        UpnpRendererState.State.FINISHED -> R.drawable.ic_play_arrow
    }

