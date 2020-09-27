package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.core.eventbus.Event
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.manager.PlayItem

class FolderClick(folder: Folder) : Event("folder_click", folder)

class MediaItemClick(playItem: PlayItem) : Event("media_item_click", playItem)
