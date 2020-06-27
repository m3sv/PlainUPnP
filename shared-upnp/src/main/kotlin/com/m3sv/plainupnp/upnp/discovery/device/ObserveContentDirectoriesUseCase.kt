package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.core.persistence.Database
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveContentDirectoriesUseCase @Inject constructor(
    private val upnpManager: UpnpManager,
    private val database: Database
) {
    operator fun invoke(): Flow<DeviceDisplayBundle> = upnpManager
        .contentDirectories
        .map { devices ->
            val deviceIndex = devices.indexOfFirst(::queryDatabaseForIdentity)
            val deviceName: String? =
                if (deviceIndex != -1) devices[deviceIndex].device.friendlyName else null

            DeviceDisplayBundle(
                devices,
                deviceIndex,
                deviceName
            )
        }

    private fun queryDatabaseForIdentity(deviceDisplay: DeviceDisplay): Boolean {
        val device = deviceDisplay.device
        return database
            .selectedDeviceQueries
            .selectDeviceByIdentity(device.fullIdentity)
            .executeAsOneOrNull() != null
    }
}
