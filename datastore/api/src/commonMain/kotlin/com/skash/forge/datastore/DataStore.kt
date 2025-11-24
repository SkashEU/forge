package com.skash.forge.datastore

import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines a contract for a simple key-value data storage system.
 *
 * This provides a generic way to interact with different data storage implementations
 * (like SharedPreferences, Proto DataStore, or a database) using a common API.
 * It operates on [DataEntry] objects, which act as typed keys.
 */
interface DataStore {
    /**
     * Observes changes to a specific data entry.
     *
     * @param T The type of the data being observed.
     * @param dataEntry The key representing the data to watch.
     * @return A [Flow] that emits the latest value whenever it changes.
     * It will emit the default value from the [DataEntry] if none is set.
     */
    fun <T> observe(dataEntry: DataEntry<T>): Flow<T>

    /**
     * Retrieves the current value of a data entry one time.
     *
     * @param T The type of the data being retrieved.
     * @param dataEntry The key for the data to get.
     * @return The stored value, or `null` if no value has been set for this key.
     */
    suspend fun <T> get(dataEntry: DataEntry<T>): T?

    /**
     * Saves or updates a value for a specific data entry.
     *
     * @param T The type of the data being saved.
     * @param dataEntry The key for the data to set.
     * @param value The value to be stored.
     */
    suspend fun <T> set(
        dataEntry: DataEntry<T>,
        value: T,
    )

    /**
     * Removes a value associated with a specific data entry.
     *
     * @param T The type of the data being deleted.
     * @param dataEntry The key for the data to delete.
     */
    suspend fun <T> delete(dataEntry: DataEntry<T>)

    /**
     * A companion object, often used for factory methods to create a default
     * instance of the [DataStore].
     */
    companion object
}
