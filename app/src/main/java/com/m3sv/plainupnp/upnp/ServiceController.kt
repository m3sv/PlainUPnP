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

package com.m3sv.plainupnp.upnp

import android.content.Context
import android.content.Intent
import org.droidupnp.legacy.cling.BaseUpnpServiceController
import org.fourthline.cling.model.meta.LocalDevice
import timber.log.Timber
import javax.inject.Inject

class ServiceController @Inject constructor(override val serviceListener: ServiceListener, private val context: Context) :
    BaseUpnpServiceController() {

    override fun pause() {
        context.unbindService(serviceListener.serviceConnection)
        super.pause()
    }

    override fun resume() {
        super.resume()
        Timber.d("Start UPnP service")
        context.bindService(
            Intent(context, UpnpService::class.java), serviceListener.serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun addDevice(localDevice: LocalDevice) {
        serviceListener.upnpService?.registry?.addDevice(localDevice)
    }

    override fun removeDevice(localDevice: LocalDevice) {
        serviceListener.upnpService?.registry?.removeDevice(localDevice)
    }
}
