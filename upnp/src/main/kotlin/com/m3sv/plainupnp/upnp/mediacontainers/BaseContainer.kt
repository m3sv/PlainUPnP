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
package com.m3sv.plainupnp.upnp.mediacontainers

import com.m3sv.plainupnp.upnp.UpnpContentRepositoryImpl
import org.fourthline.cling.support.model.WriteStatus
import org.fourthline.cling.support.model.container.Container

private fun isRoot(parentId: String?) =
    parentId?.compareTo(UpnpContentRepositoryImpl.ROOT_ID.toString()) == 0

private fun getRelativeId(id: String, parentId: String?): String =
    if (isRoot(parentId))
        id
    else
        parentId + UpnpContentRepositoryImpl.SEPARATOR + id

abstract class BaseContainer(
    id: String,
    parentID: String?,
    title: String?,
    creator: String?,
) : Container(
    getRelativeId(id, parentID),
    parentID,
    title,
    creator,
    Class("object.container"),
    0
) {
    init {
        setWriteStatus(WriteStatus.NOT_WRITABLE)

        isRestricted = true
        isSearchable = true
    }

    val rawId: String = id

    abstract override fun getChildCount(): Int?
}
