package com.m3sv.plainupnp.presentation.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.di.ViewModelFactory
import javax.inject.Inject

data class ActivityConfig(@LayoutRes val layoutId: Int)

abstract class BaseActivity<Binding : ViewDataBinding> : AppCompatActivity() {

    protected abstract val activityConfig: ActivityConfig

    protected lateinit var binding: Binding
        private set

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.dark_theme_key), false)
        ) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, activityConfig.layoutId)
    }

    protected fun requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    protected inline fun <reified T : ViewModel> getViewModel(): T =
        ViewModelProviders.of(this, viewModelFactory).get(T::class.java)

    protected inline fun <T> LiveData<T>.nonNullObserve(crossinline observer: (t: T) -> Unit) {
        this.observe(this@BaseActivity, Observer {
            it?.let(observer)
        })
    }

    companion object {
        protected const val REQUEST_READ_EXTERNAL_STORAGE = 12345
    }
}
