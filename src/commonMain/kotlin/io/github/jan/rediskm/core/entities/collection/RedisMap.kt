package io.github.jan.rediskm.core.entities.collection

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisElement
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.RedisObject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class RedisMap internal constructor(override val redisClient: RedisClient, override val key: String) : RedisObject<Map<String, String>>, RedisElement {

    override suspend fun get(): Map<String, String> {
        redisClient.sendCommand("HGETALL", key)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList().chunked(2) { (key, value) -> key to value }.toMap()
    }

    /**
     * Removes one or more hash fields from the map
     * @return the number of elements removed from the map
     */
    suspend fun remove(vararg elements: String): Long {
        redisClient.sendCommand("HDEL", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Checks whether a hash field exists in the map
     */
    suspend fun contains(element: String): Boolean {
        redisClient.sendCommand("HEXISTS", key, element)
        return redisClient.receive()!!.value == 1L
    }

    /**
     * Returns the value at the specified [key]. Null if the key does not exist.
     */
    suspend fun get(key: String): String? {
        redisClient.sendCommand("HGET", key)
        return redisClient.receive()?.value?.toString()
    }

    /**
     * Returns the value at the specified [key] and deserializes it to [T]. Null if the key does not exist.
     */
    suspend inline fun <reified T> get(key: String): T? {
        redisClient.sendCommand("HGET", key)
        return redisClient.receive()?.value?.toString()?.let { Json.decodeFromString(it) }
    }

    /**
     * Returns all values in the map
     */
    suspend fun getValues(): List<String> {
        redisClient.sendCommand("HVALS", key)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    /**
     * Increases the value of the specified [key] by [value], if [key] is a number. If the key does not exist, it is set to [value].
     * return the new value of the key
     */
    suspend fun increaseKeyBy(key: String, value: Long): Long {
        redisClient.sendCommand("HINCRBY", key, value)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Increases the value of the specified [key] by [value], if [key] is a number. If the key does not exist, it is set to [value].
     * return the new value of the key
     */
    suspend fun increaseKeyBy(key: String, value: Double): Double {
        redisClient.sendCommand("HINCRBYFLOAT", key, value)
        return redisClient.receive()!!.value.toString().toDouble()
    }

    /**
     * Returns all keys in the map
     */
    suspend fun getKeys(): List<String> {
        redisClient.sendCommand("HKEYS", key)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    /**
     * Returns the size of the map
     */
    suspend fun size(): Long {
        redisClient.sendCommand("HLEN", key)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Gets multiple values from the map
     */
    suspend fun get(vararg keys: String): List<String> {
        redisClient.sendCommand("HMGET", key, *keys)
        return redisClient.receive().fastCastTo<RedisListValue>().mapToStringList()
    }

    /**
     * Adds all key/value pairs from [map] to the map
     */
    suspend fun add(map: Map<String, String> = mapOf()) {
        redisClient.sendCommand("HMSET", map.flatMap { listOf(it.key, it.value) })
        redisClient.receive()
    }

    /**
     * Sets the value of the specified [key] to [value]
     * @return 1 if the key was set, 0 if the key already exists and was updated
     */
    suspend fun put(key: String, value: String): Int {
        redisClient.sendCommand("HSET", this.key, key, value)
        return redisClient.receive()!!.value.fastCastTo<Long>().toInt()
    }

    /**
     * Sets the value of the specified [key] to [value], if [key] doesn't already exist.
     * @return 1 if the key was set, 0 if the key already exists and the operation was not performed
     */
    suspend fun setNX(key: String, value: String): Boolean {
        redisClient.sendCommand("HSETNX", this.key, key, value)
        return redisClient.receive()!!.value == 1L
    }

}

/**
 * Returns a [RedisMap] for the specified [key]. This doesn't make a call to the redis server until you call any function on this object.
 */
fun RedisClient.getHash(key: String) = RedisMap(this, key)

/**
 * Returns a map/hash for the specified [key]
 */
suspend fun RedisClient.getMap(key: String) = getHash(key).get()

/**
 * Creates a new map/hash for the specified [key]
 *
 * **Note**: If the map already exists, the values will be added to the end of the set
 * @return A [RedisMap] for the given [key]
 */
suspend fun RedisClient.putHash(key: String, map: Map<String, String> = mapOf()) = RedisMap(this, key).apply {
    if(map.isNotEmpty()) add(map)
}

/**
 * Creates a new map/hash for the specified [key]
 *
 * **Note**: If the map already exists, the values will be added to the end of the set
 * @return A [RedisMap] for the given [key]
 */
suspend fun RedisClient.putHash(key: String, builder: MutableMap<String, String>.() -> Unit) = putHash(key, buildMap(builder))