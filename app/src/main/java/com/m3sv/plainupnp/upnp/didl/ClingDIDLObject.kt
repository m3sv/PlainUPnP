package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject


open class ClingDIDLObject internal constructor(val didlObject: DIDLObject) :
    com.m3sv.plainupnp.data.upnp.DIDLObject {
    override val dataType: String = ""

    override val title: String = didlObject.title

    override val description: String = ""

    override val count: String = ""

    override val icon: Int = android.R.color.transparent

    override val parentID: String = didlObject.parentID

    override val id: String = didlObject.id
}
