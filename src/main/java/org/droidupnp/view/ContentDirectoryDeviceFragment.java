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

package org.droidupnp.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.droidupnp.controller.upnp.IUPnPServiceController;
import org.droidupnp.model.upnp.IUPnPDevice;

import java.util.Observable;
import java.util.Observer;

public class ContentDirectoryDeviceFragment extends UpnpDeviceListFragment implements Observer {

    protected static final String TAG = ContentDirectoryDeviceFragment.class.getSimpleName();

    IUPnPServiceController controller;

    public ContentDirectoryDeviceFragment() {
        super();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        controller.getContentDirectoryDiscovery().addObserver(this);
        controller.addSelectedContentDirectoryObserver(this);
        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        controller.getContentDirectoryDiscovery().removeObserver(this);
        controller.delSelectedContentDirectoryObserver(this);
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected boolean isSelected(IUPnPDevice device) {
        if (controller != null && controller.getSelectedContentDirectory() != null)
            return device.equals(controller.getSelectedContentDirectory());

        return false;
    }

    @Override
    protected void select(IUPnPDevice device) {
        select(device, false);
    }

    @Override
    protected void select(IUPnPDevice device, boolean force) {
        controller.setSelectedContentDirectory(device, force);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        select(list.getItem(position).getDevice());
        Log.d(TAG, "Set contentDirectory to " + list.getItem(position));
    }

    @Override
    public void update(Observable observable, Object o) {
        IUPnPDevice device = controller.getSelectedContentDirectory();
        if (device == null) {
            if (getActivity() != null) // Visible
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Uncheck device
                        getListView().clearChoices();
                        list.notifyDataSetChanged();
                    }
                });
        } else {
            addedDevice(device);
        }
    }
}