package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject
import org.fourthline.cling.support.model.DIDLObject.Property.UPNP.ICON
import java.net.URI


open class ClingDIDLObject internal constructor(val didlObject: DIDLObject) :
    com.m3sv.plainupnp.data.upnp.DIDLObject {

    override val dataType: String = ""

    override val title: String = didlObject.title

    override val description: String = ""

    override val icon: URI? get() = didlObject.getFirstPropertyValue(ICON::class.java)

    override val parentID: String = didlObject.parentID

    override val id: String = didlObject.id
}
