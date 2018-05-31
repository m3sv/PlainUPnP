package com.m3sv.droidupnp.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.m3sv.droidupnp.databinding.MainFragmentBinding
import com.m3sv.droidupnp.presentation.base.BaseFragment


class MainFragment : BaseFragment() {

    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(): MainFragment {
            val fragment = MainFragment()
            val arguments = Bundle()
            fragment.arguments = arguments
            return fragment
        }
    }
}