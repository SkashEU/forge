package com.skash.forge.viewmodel

import kotlinx.cinterop.Arena
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import platform.posix.pthread_mutex_destroy
import platform.posix.pthread_mutex_init
import platform.posix.pthread_mutex_lock
import platform.posix.pthread_mutex_t
import platform.posix.pthread_mutex_unlock
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.ref.createCleaner

internal actual fun <K, V> lruCache(maxSize: Int): MutableMap<K, V> {
    return KmpLruCache(maxSize)
}

private class KmpLruCache<K, V>(
    private val maxSize: Int,
) : MutableMap<K, V> {
    private val internalMap = LinkedHashMap<K, V>()
    private val lock = Lock()

    override fun get(key: K): V? =
        lock.withLock {
            getInternal(key)
        }

    override fun put(
        key: K,
        value: V,
    ): V? =
        lock.withLock {
            putInternal(key, value)
        }

    override fun putAll(from: Map<out K, V>) =
        lock.withLock {
            from.forEach { (k, v) -> putInternal(k, v) }
        }

    override fun remove(key: K): V? =
        lock.withLock {
            internalMap.remove(key)
        }

    override fun clear() = lock.withLock { internalMap.clear() }

    private fun getInternal(key: K): V? {
        val value = internalMap.remove(key)
        if (value != null) {
            internalMap[key] = value
        }
        return value
    }

    private fun putInternal(
        key: K,
        value: V,
    ): V? {
        val previousValue = internalMap.remove(key)
        internalMap[key] = value

        if (previousValue == null && internalMap.size > maxSize) {
            val eldestKey = internalMap.keys.first()
            internalMap.remove(eldestKey)
        }
        return previousValue
    }

    override val size: Int get() = lock.withLock { internalMap.size }

    override fun containsKey(key: K): Boolean = lock.withLock { internalMap.containsKey(key) }

    override fun containsValue(value: V): Boolean = lock.withLock { internalMap.containsValue(value) }

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = lock.withLock { internalMap.entries.toMutableSet() }

    override fun isEmpty(): Boolean = lock.withLock { internalMap.isEmpty() }

    override val keys: MutableSet<K>
        get() = lock.withLock { internalMap.keys.toMutableSet() }
    override val values: MutableCollection<V>
        get() = lock.withLock { internalMap.values.toMutableList() }
}

@OptIn(ExperimentalForeignApi::class)
internal actual class Lock {
    @OptIn(ExperimentalForeignApi::class)
    private val arena = Arena()

    @OptIn(ExperimentalForeignApi::class)
    private val mutex = arena.alloc<pthread_mutex_t>()

    init {
        pthread_mutex_init(mutex.ptr, null)
    }

    actual fun lock() {
        pthread_mutex_lock(mutex.ptr)
    }

    actual fun unlock() {
        pthread_mutex_unlock(mutex.ptr)
    }

    @OptIn(ExperimentalStdlibApi::class, ExperimentalNativeApi::class)
    private val cleaner =
        createCleaner(arena) {
            pthread_mutex_destroy(mutex.ptr)
            it.clear()
        }
}