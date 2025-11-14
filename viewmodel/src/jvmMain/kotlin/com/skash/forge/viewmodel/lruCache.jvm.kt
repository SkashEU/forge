package com.skash.forge.viewmodel

internal actual fun <K, V> lruCache(maxSize: Int): MutableMap<K, V> {
    return object : LinkedHashMap<K, V>(maxSize) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return this.size > maxSize
        }
    }
}