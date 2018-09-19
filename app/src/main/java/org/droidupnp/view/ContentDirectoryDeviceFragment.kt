package org.droidupnp.view

import android.os.Bundle
import android.view.View
import android.widget.ListView
import org.droidupnp.controller.upnp.UpnpServiceController
import org.droidupnp.model.upnp.IUpnpDevice
import java.util.*


class ContentDirectoryDeviceFragment : UpnpDeviceListFragment(), Observer {

    lateinit var controller: UpnpServiceController

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        controller.contentDirectoryDiscovery.addObserver(this)
        controller.addSelectedContentDirectoryObserver(this)
    }

    override fun onDestroy() {
        controller.contentDirectoryDiscovery.removeObserver(this)
        controller.delSelectedContentDirectoryObserver(this)
        super.onDestroy()
    }

    override fun isSelected(device: IUpnpDevice?): Boolean {
        return if (controller.selectedContentDirectory != null)
            device!!.equals(controller.selectedContentDirectory)
        else false
    }

    override fun select(device: IUpnpDevice?) = select(device, false)

    override fun select(device: IUpnpDevice?, force: Boolean) = controller.setSelectedContentDirectory(device, force)

    override fun update(p0: Observable?, p1: Any?) {
        val device = controller.selectedContentDirectory
        if (device == null) {
            if (activity != null)
            // Visible
                activity.runOnUiThread {
                    // Uncheck device
                    listView.clearChoices()
                    list.notifyDataSetChanged()
                }
        } else {
            addedDevice(device)
        }
    }

    override fun onListItemClick(l: ListView?, v: View?, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)
        select(list.getItem(position)!!.device)
    }
}