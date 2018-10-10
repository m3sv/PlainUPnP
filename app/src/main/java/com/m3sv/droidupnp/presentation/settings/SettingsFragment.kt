package com.m3sv.droidupnp.presentation.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import com.m3sv.droidupnp.R


class SettingsFragment : PreferenceFragmentCompat() {


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

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