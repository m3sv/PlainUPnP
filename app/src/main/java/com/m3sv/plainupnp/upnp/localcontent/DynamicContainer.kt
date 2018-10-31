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

package com.m3sv.plainupnp.upnp.localcontent

import android.content.Context
import android.net.Uri
import org.fourthline.cling.support.model.container.Container

abstract class DynamicContainer(
    id: String?,
    parentID: String?,
    title: String?,
    creator: String?,
    baseURL: String?,
    protected val ctx: Context,
    protected var uri: Uri?
) : CustomContainer(id, parentID, title, creator, baseURL) {

    protected var where: String? = null
    protected var whereVal: Array<String> = emptyArray()
    protected var orderBy: String? = null

    abstract override fun getChildCount(): Int?

    abstract override fun getContainers(): List<Container>
}
