package com.m3sv.plainupnp.upnp.android

import android.content.Context
import com.m3sv.plainupnp.common.util.getUdn
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import com.m3sv.plainupnp.upnp.ContentRepository
import com.m3sv.plainupnp.upnp.PlainUpnpServiceConfiguration
import com.m3sv.plainupnp.upnp.R
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidUpnpServiceImpl @Inject constructor(
    context: Context,
    resourceProvider: LocalServiceResourceProvider,
    contentRepository: ContentRepository,
) : UpnpServiceImpl(PlainUpnpServiceConfiguration(), context) {

    private val localDevice by lazy {
        getLocalDevice(resourceProvider, context, contentRepository)
    }

    fun resume() {
        try {
            registry.addDevice(localDevice)
            controlPoint.search()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun pause() {
        try {
            registry.removeDevice(localDevice)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun shutdown() {
        (router as AndroidRouter).unregisterBroadcastReceiver()
        super.shutdown(false)
    }
}

private fun getLocalDevice(
    serviceResourceProvider: LocalServiceResourceProvider,
    context: Context,
    contentRepository: ContentRepository,
): LocalDevice {
    val details = DeviceDetails(
        serviceResourceProvider.settingContentDirectoryName,
        ManufacturerDetails(
            serviceResourceProvider.appName,
            serviceResourceProvider.appUrl
        ),
        ModelDetails(
            serviceResourceProvider.appName,
            serviceResourceProvider.appUrl,
            serviceResourceProvider.modelNumber,
            serviceResourceProvider.appUrl
        ),
        serviceResourceProvider.appVersion,
        serviceResourceProvider.appVersion
    )

    val validationErrors = details.validate()

    for (error in validationErrors) {
        Timber.e("Validation pb for property %s", error.propertyName)
        Timber.e("Error is %s", error.message)
    }

    val type = UDADeviceType("MediaServer", 1)

    val iconInputStream = context.resources.openRawResource(R.raw.ic_launcher_round)

    val icon = Icon(
        "image/png",
        128,
        128,
        32,
        "plainupnp-icon",
        iconInputStream
    )

    val udn = context.getUdn() ?: error("Empty UDN")

    return LocalDevice(
        DeviceIdentity(udn),
        type,
        details,
        icon,
        getLocalService(contentRepository)
    )
}

private fun getLocalService(contentRepository: ContentRepository): LocalService<ContentDirectoryService> {
    val serviceBinder = AnnotationLocalServiceBinder()
    val contentDirectoryService =
        serviceBinder.read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>

    val serviceManager = DefaultServiceManager(
        contentDirectoryService,
        ContentDirectoryService::class.java
    ).apply {
        (implementation as ContentDirectoryService).contentRepository = contentRepository
    }

    contentDirectoryService.manager = serviceManager

    return contentDirectoryService
}


