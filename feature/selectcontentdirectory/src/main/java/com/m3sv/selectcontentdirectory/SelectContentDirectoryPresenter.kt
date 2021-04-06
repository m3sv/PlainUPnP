package com.m3sv.selectcontentdirectory

import android.app.Application
import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.backgroundmode.BackgroundMode
import com.m3sv.plainupnp.backgroundmode.BackgroundModeManager
import com.m3sv.plainupnp.common.util.pass
import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import com.m3sv.plainupnp.upnp.UpnpScopeProvider
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceImpl
import com.m3sv.plainupnp.upnp.server.MediaServer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.fourthline.cling.UpnpService
import javax.inject.Inject

@HiltViewModel
class SelectContentDirectoryPresenter @Inject constructor(
    application: Application,
    backgroundModeManager: BackgroundModeManager,
    upnpService: UpnpService,
    mediaServer: MediaServer,
) : ViewModel() {

    init {
        when (backgroundModeManager.backgroundMode) {
            BackgroundMode.ALLOWED -> PlainUpnpAndroidService.start(application)
            BackgroundMode.DENIED -> pass
        }

        (application as UpnpScopeProvider).upnpScope.launch(Dispatchers.IO) {
            if (backgroundModeManager.backgroundMode == BackgroundMode.ALLOWED) {
                (upnpService as AndroidUpnpServiceImpl).resume()
                mediaServer.start()
            }
        }
    }
}
