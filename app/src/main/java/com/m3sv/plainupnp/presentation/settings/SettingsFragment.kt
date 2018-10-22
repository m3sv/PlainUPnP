package com.m3sv.plainupnp.presentation.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
import android.support.v7.preference.PreferenceFragmentCompat
import android.view.View
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.upnp.UpnpManager
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val darkThemeKey by lazy {
        getString(R.string.dark_theme_key)
    }

    @Inject
    lateinit var upnpManager: UpnpManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? DaggerAppCompatActivity)?.supportFragmentInjector()?.inject(this)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == darkThemeKey) {
            when (sharedPreferences.getBoolean(darkThemeKey, false)) {
                true -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    activity?.recreate()
                }
                false -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    activity?.recreate()
                }
            }
        } else {
            upnpManager.currentContentDirectory?.let {
                if (it.isLocal)
                    upnpManager.browseHome()
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