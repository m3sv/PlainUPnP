package com.m3sv.plainupnp.data


sealed class UpnpDeviceEvent {
    data class Added(val upnpDevice: UpnpDevice) : UpnpDeviceEvent()
    data class Removed(val upnpDevice: UpnpDevice) : UpnpDeviceEvent()
}