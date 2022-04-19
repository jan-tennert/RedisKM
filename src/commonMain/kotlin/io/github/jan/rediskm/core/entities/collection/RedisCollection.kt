package io.github.jan.rediskm.core.entities.collection

import io.github.jan.rediskm.core.entities.RedisElement
import io.github.jan.rediskm.core.entities.RedisObject

sealed interface RedisCollection <T> : RedisElement, RedisObject<T> {

    /**
     * Returns the number of elements in the collection.
     */
    suspend fun size(): Long

    /**
     * Checks whether the collection contains the given value.
     */
    suspend fun contains(element: String): Boolean

    suspend fun add(vararg elements: String): Long

    suspend fun remove(element: String): Long

}