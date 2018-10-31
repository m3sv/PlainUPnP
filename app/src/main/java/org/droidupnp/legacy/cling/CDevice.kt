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

package org.droidupnp.legacy.cling

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.types.UDAServiceType
import timber.log.Timber

class CDevice(val device: Device<*, *, *>?) : UpnpDevice {

    override fun getDisplayString(): String = device?.displayString ?: ""

    override fun getFriendlyName(): String = device?.details?.friendlyName ?: displayString

    override fun equals(otherDevice: UpnpDevice): Boolean =
        device?.let {
            it.identity.udn == (otherDevice as CDevice).device?.identity?.udn
        } ?: false

    override fun getUID(): String = device?.identity?.udn?.toString() ?: ""

    // todo finish refactoring CDevice
    override fun getExtendedInformation(): String {
        val info = StringBuilder()
        if (device!!.findServiceTypes() != null)
            for (cap in device.findServiceTypes()) {
                info.append("\n\t").append(cap.type).append(" : ").append(cap.toFriendlyString())
            }
        return info.toString()
    }

    override fun printService() {
        val services = device!!.findServices()
        for (service in services) {
            Timber.i("\t Service : $service")
            for (a in service.actions) {
                Timber.i("\t\t Action : $a")
            }
        }
    }

    override fun asService(service: String): Boolean {
        return device!!.findService(UDAServiceType(service)) != null
    }

    override fun getManufacturer(): String {
        return device!!.details.manufacturerDetails.manufacturer
    }

    override fun getManufacturerURL(): String {
        try {
            return device!!.details.manufacturerDetails.manufacturerURI.toString()
        } catch (e: Exception) {
            return ""
        }
    }

    override fun getModelName(): String {
        try {
            return device!!.details.modelDetails.modelName
        } catch (e: Exception) {
            return ""
        }

    }

    override fun getModelDesc(): String {
        try {
            return device!!.details.modelDetails.modelDescription
        } catch (e: Exception) {
            return ""
        }

    }

    override fun getModelNumber(): String {
        try {
            return device!!.details.modelDetails.modelNumber
        } catch (e: Exception) {
            return ""
        }

    }

    override fun getModelURL(): String {
        try {
            return device!!.details.modelDetails.modelURI.toString()
        } catch (e: Exception) {
            return ""
        }

    }

    override fun getXMLURL(): String {
        try {
            return device!!.details.baseURL.toString()
        } catch (e: Exception) {
            return ""
        }

    }

    override fun getPresentationURL(): String {
        try {
            return device!!.details.presentationURI.toString()
        } catch (e: Exception) {
            return ""
        }

    }

    override fun getSerialNumber(): String {
        try {
            return device!!.details.serialNumber
        } catch (e: Exception) {
            return ""
        }

    }

    override fun getUDN(): String {
        try {
            return device!!.identity.udn.toString()
        } catch (e: Exception) {
            return ""
        }

    }

    override fun isFullyHydrated(): Boolean {
        return device!!.isFullyHydrated
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val cDevice = o as CDevice?

        return if (device != null) device == cDevice!!.device else cDevice!!.device == null
    }

    override fun hashCode(): Int {
        return device?.hashCode() ?: 0
    }


    override fun toString(): String {
        return "CDevice{" +
                "device=" + device +
                '}'.toString()
    }

    override fun isLocal(): Boolean {
        return device is LocalDevice
    }
}
