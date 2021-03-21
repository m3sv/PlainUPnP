package com.m3sv.selectcontentdirectory

import android.app.Application
import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.common.BackgroundModeManager
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
        PlainUpnpAndroidService.start(application)
        (application as UpnpScopeProvider).upnpScope.launch(Dispatchers.IO) {
            if (backgroundModeManager.isAllowedToRunInBackground()) {
                (upnpService as AndroidUpnpServiceImpl).resume()
                mediaServer.start()
            }
        }
    }
}
