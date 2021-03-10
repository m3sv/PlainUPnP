package com.m3sv.selectcontentdirectory

import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.upnp.manager.Result
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SelectContentDirectoryPresenter @Inject constructor(private val upnpManaManager: UpnpManager) : ViewModel() {
    val contentDirectories: Flow<List<DeviceDisplay>> = upnpManaManager.contentDirectories

    suspend fun selectContentDirectory(item: DeviceDisplay): Deferred<Result> =
        upnpManaManager.selectContentDirectoryAsync(item.upnpDevice)

}
