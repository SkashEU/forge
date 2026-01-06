package com.skash.forge.viewmodel

internal expect fun <K, V> lruCache(maxSize: Int): MutableMap<K, V>

internal expect class Lock() {
    fun lock()

    fun unlock()
}

internal inline fun <T> Lock.withLock(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}