package com.m3sv.plainupnp.common

import android.util.LruCache
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentCache @Inject constructor() : LruCache<String, String>(10 * 1024 * 1024)
