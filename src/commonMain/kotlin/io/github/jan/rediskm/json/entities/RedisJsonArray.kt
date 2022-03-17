package io.github.jan.rediskm.json.entities

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.utils.deserialize
import io.github.jan.rediskm.core.utils.serialize
import io.github.jan.rediskm.json.params.getJson

class RedisJsonArray(override val redisClient: RedisClient, override val key: String, override val path: String) : RedisJsonElement {

    suspend inline fun <reified T> get(index: Int) = getOrNull<T>(index)!!

    suspend inline fun <reified T> getOrDefault(index: Int, default: T) = getOrNull<T>(index) ?: default

    suspend inline fun <reified T> getOrNull(index: Int): T? {
        val newPath = "$path[$index]"
        return redisClient.getJson(key, newPath)
    }

    /**
     * Adds a new element to the end of the json array
     */
    suspend inline fun <reified T> add(vararg elements: T): Long {
        redisClient.sendCommand("JSON.ARRAPPEND", key, path, *elements.map(::serialize).toTypedArray())
        return redisClient.receive()?.value as Long
    }

    /**
     * Inserts an element at the specified index
     */
    suspend inline fun <reified T> insert(index: Int, vararg elements: T): Long {
        redisClient.sendCommand("JSON.ARRINSERT", key, path, index, *elements.map(::serialize).toTypedArray())
        return redisClient.receive()?.value as Long
    }

    /**
     * Gets the index of a specific element in the json array
     * @return
     */
    suspend inline fun <reified T> indexOf(element: T): Long {
        redisClient.sendCommand("JSON.ARRINDEX", key, path, serialize(element))
        return redisClient.receive()?.value as Long
    }

    /**
     * Gets the size of the json array
     * @return the number of elements in the array
     */
    suspend fun size(): Long {
        redisClient.sendCommand("JSON.ARRLEN", key, path)
        return (redisClient.receive()?.value as? Long) ?: 0
    }

    /**
     * Removes the element at the specified index.
     * @return the removed element
     */
    suspend inline fun <reified T> remove(index: Int = -1): T {
        redisClient.sendCommand("JSON.ARRPOP", key, path, index)
        return deserialize(redisClient.receive()!!)
    }

    /**
     * Trims the array to the specified range
     * @return the new size of the array
     */
    suspend fun trim(start: Int, end: Int): Long {
        redisClient.sendCommand("JSON.ARRTRIM", key, path)
        return redisClient.receive()?.value as Long
    }

}