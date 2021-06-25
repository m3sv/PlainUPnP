package com.m3sv.plainupnp.upnp.android

import android.app.Application
import android.content.Context
import com.m3sv.plainupnp.applicationmode.ApplicationMode
import com.m3sv.plainupnp.common.preferences.PreferencesRepository
import com.m3sv.plainupnp.common.util.asApplicationMode
import com.m3sv.plainupnp.logging.Log
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import com.m3sv.plainupnp.upnp.PlainUpnpServiceConfiguration
import com.m3sv.plainupnp.upnp.R
import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.*
import org.fourthline.cling.model.types.UDADeviceType
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidUpnpServiceImpl @Inject constructor(
    application: Application,
    resourceProvider: LocalServiceResourceProvider,
    contentRepository: UpnpContentRepositoryImpl,
    private val log: Log,
    private val preferencesRepository: PreferencesRepository,
) : UpnpServiceImpl(application, PlainUpnpServiceConfiguration(), log) {

    private val scope = MainScope()

    private val localDevice by lazy {
        getLocalDevice(resourceProvider, application, contentRepository)
    }

    init {
        scope.launch {
            preferencesRepository
                .preferences
                .filterNotNull()
                .map { it.applicationMode.asApplicationMode() }
                .collect { applicationMode ->
                    try {
                        when (applicationMode) {
                            ApplicationMode.Streaming -> registry.addDevice(localDevice)
                            ApplicationMode.Player -> registry.removeDevice(localDevice)
                        }
                    } catch (e: Exception) {
                        log.e(e)
                    }
                }
        }
    }

    fun resume() {
        Timber.d("Resuming upnp service")
        try {
            if (isStreaming()) {
                registry.addDevice(localDevice)
            }
            controlPoint.search()
        } catch (e: Exception) {
            log.e(e)
        }
    }

    fun pause() {
        Timber.d("Pause upnp service")

        if (isStreaming()) {
            try {
                registry.removeDevice(localDevice)
            } catch (e: Exception) {
                log.e(e)
            }
        }
    }

    private fun isStreaming(): Boolean =
        preferencesRepository
            .preferences
            .value
            .applicationMode
            ?.asApplicationMode() == ApplicationMode.Streaming

    override fun shutdown() {
        (router as AndroidRouter).unregisterBroadcastReceiver()
        super.shutdown(false)
    }

    private fun getLocalDevice(
        serviceResourceProvider: LocalServiceResourceProvider,
        context: Context,
        contentRepository: UpnpContentRepositoryImpl,
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
            log.e("Validation pb for property ${error.propertyName}, error is ${error.message}")
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

        return LocalDevice(
            DeviceIdentity(preferencesRepository.getUdn()),
            type,
            details,
            icon,
            getLocalService(contentRepository)
        )
    }

    private fun getLocalService(contentRepository: UpnpContentRepositoryImpl): LocalService<ContentDirectoryService> {
        val serviceBinder = AnnotationLocalServiceBinder()
        val contentDirectoryService =
            serviceBinder.read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>

        contentDirectoryService.manager = DefaultServiceManager(
            contentDirectoryService,
            ContentDirectoryService::class.java
        ).also { serviceManager ->
            (serviceManager.implementation as ContentDirectoryService).let { service ->
                service.contentRepository = contentRepository
                service.log = log
            }
        }

        return contentDirectoryService
    }
}

