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
import kotlin.LazyThreadSafetyMode.NONE


class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var upnpManager: UpnpManager

    private val appVersion: String
        get() = requireActivity()
            .packageManager
            .getPackageInfo(requireActivity().packageName, 0)
            .versionName

    private val darkThemeKey by lazy(NONE) {
        getString(R.string.dark_theme_key)
    }

    private val preferenceClickListener = Preference.OnPreferenceClickListener { preference ->
        when (preference.key) {
            RATE -> {
                rateApplication()
                true
            }

            GITHUB -> {
                openGithub()
                true
            }

            PRIVACY_POLICY -> {
                openPrivacyPolicy()
                true
            }

            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as? DaggerAppCompatActivity)?.androidInjector()?.inject(this)
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(VERSION)?.summary = appVersion
        findPreference<Preference>(RATE)?.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(GITHUB)?.onPreferenceClickListener = preferenceClickListener
        findPreference<Preference>(PRIVACY_POLICY)?.onPreferenceClickListener =
            preferenceClickListener

        applyBottomMargin()
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
            val defaultNightMode = if (sharedPreferences.getBoolean(darkThemeKey, false))
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO

            AppCompatDelegate.setDefaultNightMode(defaultNightMode)
        }
    }

    private fun openPrivacyPolicy() {
        val browserIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.freeprivacypolicy.com/privacy/view/bf0284b77ca1af94b405030efd47d254")
            )
        startActivity(browserIntent)
    }

    private fun openGithub() {
        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/m3sv/PlainUPnP"))
        startActivity(browserIntent)
    }

    private fun openPlayMarketFallBack() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + this.activity?.packageName)
            )
        )
    }

    private fun openPlayMarket() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + this.activity?.packageName)
            )
        )
    }

    private fun rateApplication() {
        try {
            openPlayMarket()
        } catch (e: Throwable) {
            openPlayMarketFallBack()
        }
    }

    private fun applyBottomMargin() {
        listView.layoutParams = (listView.layoutParams as ViewGroup.MarginLayoutParams).apply {
            bottomMargin = 64.toPx()
        }
    }

    private fun Int.toPx(): Int =
        (this * this@SettingsFragment.resources.displayMetrics.density).toInt()

    private companion object {
        private const val VERSION = "version"
        private const val RATE = "rate"
        private const val GITHUB = "github"
        private const val PRIVACY_POLICY = "privacy_policy"
    }
}
