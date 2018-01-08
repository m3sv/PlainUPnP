package com.m3sv.droidupnp.presentation.settings

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.databinding.SettingsActivityBinding
import com.m3sv.presentation.base.BaseActivity


class SettingsActivity : BaseActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.settings_activity)
        setToolbarWithBackButton(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = "Hello world"
    }

    companion object {
        val CONTENT_DIRECTORY_SERVICE = "pref_content_directory_service"
    }
}