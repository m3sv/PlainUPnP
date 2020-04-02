package com.m3sv.plainupnp.upnp

import android.content.Context
import android.preference.PreferenceManager
import com.m3sv.plainupnp.common.ContentCache
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import org.fourthline.cling.model.types.UDN
import timber.log.Timber
import java.util.*

class LocalUpnpDevice {
    companion object {
        fun getLocalDevice(
            serviceResourceProvider: LocalServiceResourceProvider,
            context: Context,
            contentCache: ContentCache
        ): LocalDevice {
            val details = DeviceDetails(
                serviceResourceProvider.settingContentDirectoryName,

                ManufacturerDetails(
                    serviceResourceProvider.appName,
                    serviceResourceProvider.appUrl
                ),
                ModelDetails(
                    serviceResourceProvider.appName,
                    serviceResourceProvider.appUrl
                ),
                serviceResourceProvider.appName,
                serviceResourceProvider.appVersion
            )

            val validationErrors = details.validate()

            for (error in validationErrors) {
                Timber.e("Validation pb for property %s", error.propertyName)
                Timber.e("Error is %s", error.message)
            }

            val type = UDADeviceType("MediaServer", 1)

            return LocalDevice(
                DeviceIdentity(UDN.valueOf(UUID(0, 10).toString())),
                type,
                details,
                getLocalService(
                    context,
                    contentCache
                )
            )
        }

        private fun getLocalService(
            context: Context,
            contentCache: ContentCache
        ) = (AnnotationLocalServiceBinder()
            .read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>)
            .apply {
                manager = DefaultServiceManager(this, ContentDirectoryService::class.java).apply {
                    (implementation as ContentDirectoryService).let { service ->
                        service.context = context
                        service.baseURL = "${getLocalIpAddress(context).hostAddress}:$PORT"
                        service.sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                        service.cache = contentCache
                    }
                }
            }
    }
}
