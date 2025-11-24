package com.skash.forge.viewmodel

internal expect fun <K, V> lruCache(maxSize: Int): MutableMap<K, V>