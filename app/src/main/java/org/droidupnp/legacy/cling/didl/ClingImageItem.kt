package org.droidupnp.legacy.cling.didl

import com.m3sv.plainupnp.R
import org.fourthline.cling.support.model.item.ImageItem
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class ClingImageItem(item: ImageItem) : ClingDIDLItem(item) {

    override fun getDataType(): String = "image/*"

    override fun getDescription(): String = didlObject.resources
        ?.takeIf { it.isNotEmpty() }
        ?.get(0)
        ?.resolution ?: ""

    override fun getCount(): String {
        try {
            val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            val date = formatIn.parse((didlObject as ImageItem).date)
            val formatOut = DateFormat.getDateTimeInstance()
            return formatOut.format(date)
        } catch (e: Exception) {
            Timber.e(e)
        }

        return ""
    }

    override fun getIcon(): Int = R.drawable.ic_action_picture
}