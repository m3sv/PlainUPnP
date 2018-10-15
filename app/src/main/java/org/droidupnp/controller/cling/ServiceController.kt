/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien></aurelien>@chabot.fr>
 *
 *
 * This file is part of DroidUPNP.
 *
 *
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */

package org.droidupnp.controller.cling

import android.content.Context
import android.content.Intent

import org.droidupnp.model.cling.UpnpService
import org.droidupnp.model.cling.UpnpServiceController
import org.fourthline.cling.model.meta.LocalDevice

import javax.inject.Inject

import timber.log.Timber

class ServiceController @Inject constructor(private val context: Context) : UpnpServiceController() {

    private val upnpServiceListener: ServiceListener = ServiceListener(context)

    override fun getServiceListener(): ServiceListener {
        return upnpServiceListener
    }

    override fun pause() {
        context.unbindService(upnpServiceListener.getServiceConnection())
        super.pause()
    }

    override fun resume() {
        super.resume()
        // This will start the UPnP service if it wasn't already started
        Timber.d("Start upnp service")
        context.bindService(
            Intent(context, UpnpService::class.java), upnpServiceListener.getServiceConnection(),
            Context.BIND_AUTO_CREATE
        )
    }

    override fun addDevice(localDevice: LocalDevice) {
        upnpServiceListener.getUpnpService().registry.addDevice(localDevice)
    }

    override fun removeDevice(localDevice: LocalDevice) {
        upnpServiceListener.getUpnpService().registry.removeDevice(localDevice)
    }
}
