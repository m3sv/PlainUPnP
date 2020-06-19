package com.m3sv.plainupnp.data.upnp

import java.net.URI

interface DIDLObject {
    val dataType: String
    val title: String
    val description: String
    val icon: URI?
    val parentID: String
    val id: String
}
