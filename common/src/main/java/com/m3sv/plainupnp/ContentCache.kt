package com.m3sv.plainupnp

import android.util.LruCache

class ContentCache : LruCache<String, String>(10 * 1024 * 1024) {
}