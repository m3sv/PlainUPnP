package com.m3sv.plainupnp.upnp.didl

import com.m3sv.plainupnp.upnp.R
import org.fourthline.cling.support.model.item.ImageItem

class ClingImageItem(item: ImageItem) : ClingDIDLItem(item) {

    override val dataType: String = "image/*"

    override val icon: Int = R.drawable.ic_action_picture
}
