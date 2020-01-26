package com.m3sv.plainupnp.presentation.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.m3sv.plainupnp.App
import com.m3sv.plainupnp.upnp.UpnpManager
import javax.inject.Inject

private val Fragment.packageName
    get() = requireActivity().packageName

private val Fragment.appVersion: String
    get() = requireActivity()
        .packageManager
        .getPackageInfo(requireActivity().packageName, 0)
        .versionName

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    Preference.OnPreferenceClickListener {

    @Inject
    lateinit var upnpManager: UpnpManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        inject()
        super.onViewCreated(view, savedInstanceState)

        findPreference<Preference>(VERSION)?.summary = appVersion
        findPreference<Preference>(RATE)?.onPreferenceClickListener = this
        findPreference<Preference>(GITHUB)?.onPreferenceClickListener = this
        findPreference<Preference>(PRIVACY_POLICY)?.onPreferenceClickListener = this
    }

    private fun inject() {
        DaggerSettingsComponent
            .factory()
            .create((requireActivity().applicationContext as App).appComponent)
            .inject(this)
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
        val darkThemeKey = getString(com.m3sv.plainupnp.common.R.string.dark_theme_key)
        when (key) {
            darkThemeKey -> {
                val defaultNightMode = if (isDarkThemeSet(sharedPreferences, darkThemeKey))
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO

                AppCompatDelegate.setDefaultNightMode(defaultNightMode)
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val intent: Intent? = when (preference.key) {
            RATE -> rateApplication()
            GITHUB -> githubIntent()
            PRIVACY_POLICY -> privacyPolicyIntent()
            else -> null
        }

        if (intent != null)
            startActivity(intent)

        return intent != null
    }

    private fun privacyPolicyIntent() = Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_LINK))

    private fun githubIntent() = Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_LINK))

    private fun playMarketIntent() =
        Intent(Intent.ACTION_VIEW, Uri.parse("$MARKET_PREFIX$packageName"))

    private fun playMarketFallbackIntent() =
        Intent(Intent.ACTION_VIEW, Uri.parse("$PLAY_STORE_PREFIX$packageName"))

    private fun rateApplication(): Intent = try {
        playMarketIntent()
    } catch (e: Throwable) {
        playMarketFallbackIntent()
    }

    private fun isDarkThemeSet(
        sharedPreferences: SharedPreferences,
        darkThemeKey: String
    ) = sharedPreferences.getBoolean(darkThemeKey, false)

    private companion object {
        private const val VERSION = "version"
        private const val RATE = "rate"
        private const val GITHUB = "github"
        private const val PRIVACY_POLICY = "privacy_policy"

        private const val GITHUB_LINK = "https://github.com/m3sv/PlainUPnP"
        private const val PRIVACY_POLICY_LINK =
            "https://www.freeprivacypolicy.com/privacy/view/bf0284b77ca1af94b405030efd47d254"
        private const val PLAY_STORE_PREFIX = "http://play.google.com/store/apps/details?id="
        private const val MARKET_PREFIX = "market://details?id="
    }
}
