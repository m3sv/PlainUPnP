package com.m3sv.plainupnp.upnp.cleanslate

import android.content.Context
import android.preference.PreferenceManager
import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.upnp.ContentDirectoryService
import com.m3sv.plainupnp.upnp.PORT
import org.fourthline.cling.binding.annotations.AnnotationLocalServiceBinder
import org.fourthline.cling.model.DefaultServiceManager
import org.fourthline.cling.model.meta.LocalService
import java.net.InetAddress


class LocalService(
    private val context: Context,
    private val localAddress: InetAddress,
    private val contentCache: ContentCache
) {
    fun getLocalService() = (AnnotationLocalServiceBinder()
        .read(ContentDirectoryService::class.java) as LocalService<ContentDirectoryService>)
        .apply {
            manager = DefaultServiceManager(
                this,
                ContentDirectoryService::class.java
            ).apply {
                with(implementation as ContentDirectoryService) {
                    context = this@LocalService.context
                    baseURL = "${localAddress.hostAddress}:$PORT"
                    sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                    cache = contentCache
                }
            }
        }

}
