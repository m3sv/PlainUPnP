/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.droidupnp.model.cling.didl;

import org.droidupnp.model.upnp.didl.IDIDLObject;
import org.fourthline.cling.support.model.DIDLObject;

public class ClingDIDLObject implements IDIDLObject {

    protected DIDLObject item;

    ClingDIDLObject(DIDLObject item) {
        this.item = item;
    }

    public DIDLObject getObject() {
        return item;
    }

    @Override
    public String getDataType() {
        return "";
    }

    @Override
    public String getTitle() {
        return item.getTitle();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getCount() {
        return "";
    }

    @Override
    public int getIcon() {
        return android.R.color.transparent;
    }

    @Override
    public String getParentID() {
        return item.getParentID();
    }

    @Override
    public String getId() {
        return item.getId();
    }
}
