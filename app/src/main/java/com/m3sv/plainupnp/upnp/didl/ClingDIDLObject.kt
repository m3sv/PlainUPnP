package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject


open class ClingDIDLObject internal constructor(val didlObject: DIDLObject) :
    com.m3sv.plainupnp.data.upnp.DIDLObject {
    override val dataType: String
        get() = ""
    override val title: String
        get() = didlObject.title
    override val description: String
        get() = ""
    override val count: String
        get() = ""
    override val icon: Int
        get() = android.R.color.transparent
    override val parentID: String
        get() = didlObject.parentID
    override val id: String
        get() = didlObject.id
}
