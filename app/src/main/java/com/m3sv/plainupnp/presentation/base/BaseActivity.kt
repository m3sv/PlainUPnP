package com.m3sv.plainupnp.presentation.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.NavigationHost
import com.m3sv.plainupnp.di.ViewModelFactory
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


abstract class BaseActivity : DaggerAppCompatActivity(), NavigationHost {

    protected val disposables = CompositeDisposable()

    protected val REQUEST_READ_EXTERNAL_STORAGE = 12345

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        if (PreferenceManager
                        .getDefaultSharedPreferences(this)
                        .getBoolean(getString(R.string.dark_theme_key), false)) {
            setTheme(R.style.MainActivityThemeDark)
        } else {
            setTheme(R.style.MainActivityThemeLight)
        }
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    protected fun requestReadStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }
    }

    override fun navigateTo(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        val transaction =
                supportFragmentManager.beginTransaction().replace(R.id.container, fragment)

        if (addToBackStack)
            transaction.addToBackStack(null)

        transaction.commit()
    }

    private var lastBackClick = System.currentTimeMillis()

    protected fun doubleTapExit() {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastBackClick < 500)
            finishAndRemoveTask()

        lastBackClick = currentTime
        Toast.makeText(this, R.string.to_exit, Toast.LENGTH_SHORT).show()
    }

    protected inline fun <reified T : ViewModel> getViewModel(): T =
            ViewModelProviders.of(this, viewModelFactory).get(T::class.java)

    protected inline fun <T> LiveData<T>.nonNullObserve(crossinline observer: (t: T) -> Unit) {
        this.observe(this@BaseActivity, Observer {
            it?.let(observer)
        })
    }
}