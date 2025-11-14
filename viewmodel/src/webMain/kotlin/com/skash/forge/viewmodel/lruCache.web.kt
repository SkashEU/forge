package com.skash.forge.viewmodel

internal actual fun <K, V> lruCache(maxSize: Int): MutableMap<K, V> {
    return KmpLruCache(maxSize)
}

private class KmpLruCache<K, V>(private val maxSize: Int) : MutableMap<K, V> {
    private val internalMap = LinkedHashMap<K, V>()

    override fun get(key: K): V? {
        val value = internalMap.remove(key)
        if (value != null) {
            internalMap[key] = value
        }
        return value
    }

    override fun put(key: K, value: V): V? {
        val previousValue = internalMap.remove(key)
        internalMap[key] = value

        if (previousValue == null && internalMap.size > maxSize) {
            val eldestKey = internalMap.keys.first()
            internalMap.remove(eldestKey)
        }

        return previousValue
    }

    override val size: Int get() = internalMap.size
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = internalMap.entries
    override val keys: MutableSet<K> get() = internalMap.keys
    override val values: MutableCollection<V> get() = internalMap.values
    override fun containsKey(key: K): Boolean = internalMap.containsKey(key)
    override fun containsValue(value: V): Boolean = internalMap.containsValue(value)
    override fun isEmpty(): Boolean = internalMap.isEmpty()
    override fun clear() = internalMap.clear()
    override fun remove(key: K): V? = internalMap.remove(key)
    override fun putAll(from: Map<out K, V>) = from.forEach { (k, v) -> put(k, v) }
}