package com.m3sv.plainupnp.upnp.didl

import com.m3sv.upnp.R
import org.fourthline.cling.support.model.item.ImageItem
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ClingImageItem(item: ImageItem) : ClingDIDLItem(item) {

    override val dataType: String = "image/*"

    override val description: String = didlObject.resources
            ?.takeIf { it.isNotEmpty() }
            ?.get(0)
            ?.resolution ?: ""

    override val count: String
        get() {
            try {
                val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val date = formatIn.parse((didlObject as ImageItem).date)
                val formatOut = DateFormat.getDateTimeInstance()
                return formatOut.format(date)
            } catch (e: Exception) {
//                Timber.e(e)
            }

            return ""
        }

    override val icon: Int = R.drawable.ic_action_picture
}