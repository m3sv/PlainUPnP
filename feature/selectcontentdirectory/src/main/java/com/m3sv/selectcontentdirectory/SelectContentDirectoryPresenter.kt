package com.m3sv.selectcontentdirectory

import android.app.Application
import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.backgroundmode.BackgroundMode
import com.m3sv.plainupnp.backgroundmode.BackgroundModeManager
import com.m3sv.plainupnp.common.util.pass
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectContentDirectoryPresenter @Inject constructor(
    application: Application,
    backgroundModeManager: BackgroundModeManager,
) : ViewModel() {

    init {
        when (backgroundModeManager.backgroundMode) {
            BackgroundMode.ALLOWED -> PlainUpnpAndroidService.start(application)
            BackgroundMode.DENIED -> pass
        }
    }
}
