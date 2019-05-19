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

package com.m3sv.plainupnp.upnp.didl


import com.m3sv.plainupnp.data.upnp.DIDLContainer
import com.m3sv.plainupnp.upnp.R
import org.fourthline.cling.support.model.container.Container

class ClingDIDLContainer(item: Container) : ClingDIDLObject(item),
        DIDLContainer {

    override val childCount: Int =
            didlObject.takeIf { it is Container }?.let { (it as Container).childCount } ?: 0

    override val count: String = Integer.toString(childCount)

    override val icon: Int = R.drawable.ic_action_collection
}
