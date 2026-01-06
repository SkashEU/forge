package com.skash.forge.viewmodel

import java.util.Collections
import java.util.LinkedHashMap
import java.util.concurrent.locks.ReentrantLock

internal actual fun <K, V> lruCache(maxSize: Int): MutableMap<K, V> {
    val cache =
        object : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean = this.size > maxSize
        }

    return Collections.synchronizedMap(cache)
}

internal actual typealias Lock = ReentrantLock