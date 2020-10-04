package com.m3sv.plainupnp.presentation.inappplayer

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.m3sv.plainupnp.presentation.inappplayer.databinding.ImageFragmentBinding

class ImageFragment : Fragment(R.layout.image_fragment) {

    private val binding: ImageFragmentBinding by lazy { ImageFragmentBinding.bind(requireView()) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Glide.with(this)
            .load(requireNotNull(requireArguments().getString(IMAGE_URL)))
            .into(binding.root)
    }

    companion object {
        private const val IMAGE_URL = "image_url"

        fun newInstance(imageUrl: String): ImageFragment = ImageFragment().apply {
            arguments = bundleOf(IMAGE_URL to imageUrl)
        }
    }
}
