package com.m3sv.droidupnp.presentation.main

import android.app.Fragment
import android.os.Bundle
import com.m3sv.droidupnp.presentation.base.BaseFragment


class MainFragment : BaseFragment() {


    companion object {
        fun newInstance(): MainFragment {
            val fragment = MainFragment()
            val arguments = Bundle()
            fragment.arguments = arguments
            return fragment
        }
    }
}