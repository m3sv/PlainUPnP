package com.m3sv.plainupnp.data.upnp


import com.m3sv.plainupnp.data.upnp.DIDLObject

data class DIDLObjectDisplay(val didlObject: DIDLObject) {

    val title: String
        get() = didlObject.title

    val description: String
        get() = didlObject.description

    val count: String
        get() = didlObject.count

    val icon: Int
        get() = didlObject.icon
}
