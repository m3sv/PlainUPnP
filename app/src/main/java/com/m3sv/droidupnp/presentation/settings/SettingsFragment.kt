package com.m3sv.droidupnp.presentation.settings

import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.databinding.SettingsFragmentBinding
import com.m3sv.droidupnp.presentation.base.BaseActivity
import com.m3sv.droidupnp.presentation.base.BaseFragment
import com.m3sv.droidupnp.presentation.base.THEME_KEY


class SettingsFragment : BaseFragment() {


    private lateinit var binding: SettingsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.changeTheme.setOnClickListener {
            with(requireActivity() as BaseActivity) {
                if (isLightTheme) {
                    setTheme(R.style.AppTheme_Dark)
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(THEME_KEY, false).apply()
                    recreate()
                } else {
                    setTheme(R.style.AppTheme)
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean(THEME_KEY, true).apply()
                    recreate()
                }
            }
        }
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