package com.m3sv.plainupnp.upnp.folder

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

sealed class Folder(open val id: String, open val title: String) : Parcelable {

    @Parcelize
    data class Root(
        override val id: String,
        override val title: String,
    ) : Folder(id, title)

    @Parcelize
    data class SubFolder(
        override val id: String,
        override val title: String,
    ) : Folder(id, title)
}
