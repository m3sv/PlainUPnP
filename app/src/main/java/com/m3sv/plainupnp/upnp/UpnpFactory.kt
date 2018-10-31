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
import org.droidupnp.legacy.cling.UpnpRendererState
import org.droidupnp.legacy.upnp.Factory
import org.droidupnp.legacy.upnp.IContentDirectoryCommand
import org.droidupnp.legacy.upnp.IRendererCommand
import javax.inject.Inject

class UpnpFactory @Inject constructor(private val controller: UpnpServiceController) : Factory {

    override fun createContentDirectoryCommand(): IContentDirectoryCommand? =
        (controller.serviceListener as ServiceListener).upnpService
            ?.controlPoint
            ?.let {
                ContentDirectoryCommand(it, controller)
            }

    override fun createRendererCommand(rendererState: UpnpRendererState?): IRendererCommand? =
        (controller.serviceListener as ServiceListener).upnpService
            ?.controlPoint
            ?.let {
                rendererState?.let { rs -> RendererCommand(controller, it, rs) }
            }

    override fun createUpnpServiceController(ctx: Context): UpnpServiceController = controller

    override fun createRendererState(): UpnpRendererState =
        org.droidupnp.legacy.cling.UpnpRendererState()
}
