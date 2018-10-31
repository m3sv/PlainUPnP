package org.droidupnp.legacy.cling.didl

import org.fourthline.cling.support.model.DIDLObject


open class ClingDIDLObject internal constructor(val didlObject: DIDLObject) :
    com.m3sv.plainupnp.data.upnp.DIDLObject {

    override fun getDataType(): String = ""

    override fun getTitle(): String = didlObject.title

    override fun getDescription(): String = ""

    override fun getCount(): String = ""

    override fun getIcon(): Int = android.R.color.transparent

    override fun getParentID(): String = didlObject.parentID

    override fun getId(): String = didlObject.id
}
