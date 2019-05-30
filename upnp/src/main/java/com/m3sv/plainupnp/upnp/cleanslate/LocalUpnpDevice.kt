package com.m3sv.plainupnp.upnp.cleanslate

import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import timber.log.Timber
import java.util.*

class LocalUpnpDevice(private val serviceResourceProvider: LocalServiceResourceProvider,
                      private val localService: LocalService) {

    operator fun invoke(): LocalDevice {
        val details = DeviceDetails(
                serviceResourceProvider.settingContentDirectoryName,
                ManufacturerDetails(
                        serviceResourceProvider.appName,
                        serviceResourceProvider.appUrl
                ),
                ModelDetails(serviceResourceProvider.appName, serviceResourceProvider.appUrl),
                serviceResourceProvider.appName, serviceResourceProvider.appVersion
        )


        val validationErrors = details.validate()

        for (error in validationErrors) {
            Timber.e("Validation pb for property " + error.propertyName)
            Timber.e("Error is " + error.message)
        }

        val type = UDADeviceType("MediaServer", 1)

        return LocalDevice(DeviceIdentity(udn), type, details, localService())
    }

    private val udn: UDN by lazy(mode = LazyThreadSafetyMode.NONE) {
        UDN.valueOf(UUID(0, 10).toString())
    }
}