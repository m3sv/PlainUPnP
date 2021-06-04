package com.m3sv.selectcontentdirectory

import android.app.Application
import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectContentDirectoryPresenter @Inject constructor(application: Application) : ViewModel() {

    init {
        PlainUpnpAndroidService.start(application)
    }
}
