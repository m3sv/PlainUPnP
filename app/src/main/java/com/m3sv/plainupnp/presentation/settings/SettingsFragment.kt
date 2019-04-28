package com.m3sv.plainupnp.presentation.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.upnp.UpnpManager
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class SettingsFragment : PreferenceFragmentCompat(),
        SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener {

    @Inject
    lateinit var upnpManager: UpnpManager

    private val darkThemeKey by lazy(LazyThreadSafetyMode.NONE) { getString(R.string.dark_theme_key) }

    private val appVersion: String
        get() = activity
                ?.packageManager
                ?.getPackageInfo(activity?.packageName, 0)
                ?.versionName
                ?: "1.0"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? DaggerAppCompatActivity)?.supportFragmentInjector()?.inject(this)
        super.onViewCreated(view, savedInstanceState)

        findPreference("version").apply { summary = appVersion }
        findPreference("rate").also { it.onPreferenceClickListener = this }
        findPreference("github").also { it.onPreferenceClickListener = this }
        findPreference("privacy_policy").also { it.onPreferenceClickListener = this }

        listView.layoutParams = (listView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            bottomMargin = 64.toPx()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
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

    override fun onPreferenceClick(preference: Preference): Boolean = when (preference.key) {
        "rate" -> {
            try {
                startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.activity?.packageName))
                )
            } catch (e: Throwable) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + this.activity?.packageName)))
            }

            true
        }

        "github" -> {
            val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/m3sv/PlainUPnP"))
            startActivity(browserIntent)
            true
        }

        "privacy_policy" -> {
            val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://www.freeprivacypolicy.com/privacy/view/bf0284b77ca1af94b405030efd47d254"))
            startActivity(browserIntent)
            true
        }

        else -> false
    }

    private fun Int.toPx(): Int =
            (this * this@SettingsFragment.resources.displayMetrics.density).toInt()

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
