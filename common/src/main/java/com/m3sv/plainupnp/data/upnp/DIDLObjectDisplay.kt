package com.m3sv.plainupnp.data.upnp


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
