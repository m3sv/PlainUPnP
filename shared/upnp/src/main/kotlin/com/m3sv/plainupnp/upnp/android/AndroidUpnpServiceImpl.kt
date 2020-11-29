package com.m3sv.plainupnp.upnp.android

import android.content.Context
import android.content.SharedPreferences
import com.m3sv.plainupnp.common.util.getUdn
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import com.m3sv.plainupnp.upnp.R
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider
import com.m3sv.plainupnp.upnp.util.PORT
import com.m3sv.plainupnp.upnp.util.getLocalIpAddress
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import timber.log.Timber

class AndroidUpnpServiceImpl(
    context: Context,
    configuration: AndroidUpnpServiceConfiguration,
    resourceProvider: LocalServiceResourceProvider,
    sharedPreferences: SharedPreferences,
) : UpnpServiceImpl(configuration, context) {

    private val localDevice by lazy {
        getLocalDevice(resourceProvider, context, sharedPreferences)
    }

    fun resume() {
        registry.addDevice(localDevice)
        controlPoint.search()
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
    sharedPreferences: SharedPreferences,
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
        getLocalService(context, sharedPreferences)
    )
}

private fun getLocalService(
    context: Context,
    sharedPreferences: SharedPreferences,
): LocalService<ContentDirectoryService> {
    val serviceBinder = AnnotationLocalServiceBinder()
    val contentDirectoryService =
        serviceBinder.read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>

    val serviceManager = DefaultServiceManager(
        contentDirectoryService,
        ContentDirectoryService::class.java
    ).apply {
        (implementation as ContentDirectoryService).let { service ->
            service.context = context
            service.baseURL = "${
                getLocalIpAddress(
                    context
                ).hostAddress
            }:$PORT"
            service.sharedPref = sharedPreferences
        }
    }

    contentDirectoryService.manager = serviceManager

    return contentDirectoryService
}


