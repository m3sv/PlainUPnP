package com.m3sv.droidupnp.presentation.settings

import android.os.Bundle
import com.m3sv.droidupnp.presentation.base.BaseFragment


class SettingsFragment : BaseFragment() {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName

        fun newInstance(): SettingsFragment {
            val fragment = SettingsFragment()
            val arguments = Bundle()
            fragment.arguments = arguments
            return fragment
        }
    }
}