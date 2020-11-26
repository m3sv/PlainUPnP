package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.core.eventbus.Event
import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.manager.PlayItem

class FolderClick(folder: Folder) : Event(folder)

class MediaItemClick(playItem: PlayItem) : Event(playItem)

class MediaItemLongClick(playItem: PlayItem) : Event(playItem)
