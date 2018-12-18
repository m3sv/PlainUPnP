package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.UpnpManager
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(private val upnpManager: UpnpManager) :
    BaseViewModel(), UpnpManager by upnpManager