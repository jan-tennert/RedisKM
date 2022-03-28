package io.github.jan.rediskm.core.entities.collection

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisException
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.utils.deserialize
import io.github.jan.rediskm.core.utils.serialize
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

class RedisList internal constructor(override val redisClient: RedisClient, override val key: String) : RedisCollection<List<String>> {

    override suspend fun get() = subList(0, -1)

    /**
     * Removes and returns the first element of the list.
     * @return The element that was removed from the list.
     */
    suspend fun popFirst(): String? {
        redisClient.sendCommand("LPOP", key)
        return redisClient.receive()?.value?.toString()
    }

    /**
     * Removes and returns the last element of the list.
     * @return The element that was removed from the list.
     */
    suspend fun popLast(): String? {
        redisClient.sendCommand("RPOP", key)
        return redisClient.receive()?.value?.toString()
    }

    /**
     * Removes the last element in a list, appends it to [destination] and returns it
     * @return The element that was removed from the list.
     */
    suspend fun popAndPush(destination: String): String? {
        redisClient.sendCommand("RPOPLPUSH", key, destination)
        return redisClient.receive()?.value?.toString()
    }

    /**
     * Gets an element at the specified [index] and serializes it to a [T]
     * @return the serialized element
     */
    suspend inline fun <reified T> get(index: Int): T {
        redisClient.sendCommand("LINDEX", key, index)
        return deserialize(redisClient.receive()!!)
    }

    /**
     * Inserts [newValue] before [existingValue]
     * @return The length of the list after the insert operation or -1 if [existingValue] was not found.
     */
    suspend fun insertBefore(existingValue: String, newValue: String): Long {
        redisClient.sendCommand("LINSERT", key, "BEFORE", existingValue, newValue)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Inserts [newValue] after [existingValue]
     * @return The length of the list after the insert operation or -1 if [existingValue] was not found.
     */
    suspend fun insertAfter(existingValue: String, newValue: String): Long {
        redisClient.sendCommand("LINSERT", key, "AFTER", existingValue, newValue)
        return redisClient.receive()!!.value as Long
    }

    override suspend fun size(): Long {
        redisClient.sendCommand("LLEN", key)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Adds string [elements] at the head of the list
     * @return The length of the list after the operation
     */
    suspend fun addFirst(vararg elements: String): Long {
        redisClient.sendCommand("LPUSH", key, *elements)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Adds serialized [elements] at the head of the list
     * @return The length of the list after the operation
     */
    suspend inline fun <reified T> addFirst(vararg elements: T): Long {
        redisClient.sendCommand("LPUSH", key, *elements.map(::serialize).toTypedArray())
        return redisClient.receive()!!.value as Long
    }

    /**
     * Only adds these elements to the head of the list, if they are not already in the list
     * @return The length of the list after the operation
     */
    suspend inline fun <reified T> addFirstX(vararg elements: T): Long {
        redisClient.sendCommand("LPUSHX", key, *elements.map(::serialize).toTypedArray())
        return redisClient.receive()!!.value as Long
    }

    /**
     * Returns a sub list with the items from the list between [start] and [end]
     *
     * **Note**: This operation does not edit the original list. Use [trim] if you want to edit the original list
     * @return The sub list
     */
    suspend fun subList(start: Int, end: Int): List<String> {
        redisClient.sendCommand("LRANGE", key, start, end)
        return (redisClient.receive() as RedisListValue).mapToStringList()
    }

    /**
     * Removes [count] amount of [value] elements from the list
     * @return The length of the list after the operation
     */
    suspend fun remove(value: String, count: Int = 0): Long {
        redisClient.sendCommand("LREM", key, count, value)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Replaces the element at [index] with [newValue]
     */
    suspend fun set(index: Int, newValue: String) {
        redisClient.sendCommand("LSET", key, index, newValue)
        redisClient.receive()
    }

    /**
     * Trims the list to the specified range
     *
     * **Note**: This operation does edit the original list. Use [subList] if you want to get a copy
     */
    suspend fun trim(start: Int, end: Int) {
        redisClient.sendCommand("LTRIM", key, start, end)
        redisClient.receive()
    }

    /**
     * Adds multiple elements on the right side of the list
     * @return The length of the list after the operation
     */
    suspend fun add(vararg values: String): Long {
        redisClient.sendCommand("RPUSH", key, *values)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Adds multiple elements on the right side of the list
     * @return The length of the list after the operation
     */
    suspend inline fun <reified T> add(vararg values: T): Long {
        redisClient.sendCommand("RPUSH", key, *values.map(::serialize).toTypedArray())
        return redisClient.receive()!!.value as Long
    }

    /**
     * Adds multiple elements on the right side of the list, if they are not already in the list
     * @return The length of the list after the operation
     */
    suspend fun addX(vararg values: String): Long {
        redisClient.sendCommand("RPUSHX", key, *values)
        return redisClient.receive()!!.value as Long
    }

    /**
     * Adds multiple elements on the right side of the list, if they are not already in the list
     * @return The length of the list after the operation
     */
    suspend inline fun <reified T> addX(vararg values: T): Long {
        redisClient.sendCommand("RPUSHX", key, *values.map(::serialize).toTypedArray())
        return redisClient.receive()!!.value as Long
    }

    override suspend fun contains(element: String) = get().contains(element)

}

/**
 * Returns a serialized [List] of [T] for the given [key]
 */
suspend inline fun <reified T> RedisClient.getList(key: String) : List<T> {
    val list = getList(key).get()
    return when(T::class) {
        String::class -> list.map { it as T }
        Int::class -> list.map { it.toInt() as T }
        Long::class -> list.map { it.toLong() as T }
        Float::class -> list.map { it.toFloat() as T }
        Double::class -> list.map { it.toDouble() as T }
        else -> {
            val serializer = serializerOrNull(typeOf<T>()) ?: throw RedisException("Unsupported type: ${T::class}")
            list.map { Json.decodeFromString(serializer, it) as T }
        }
    }
}

/**
 * Returns a [RedisList] for the given [key]. This doesn't make a call to the redis server until you call any function on this object.
 */
fun RedisClient.getList(key: String) = RedisList(this, key)

/**
 * Creates a new [RedisList] with the given [key] and adds [values] to it.
 *
 * **Note**: If the list already exists, the values will be added to the end of the list
 * @return A [RedisList] for the given [key]
 */
suspend inline fun <reified T> RedisClient.putList(key: String, vararg values: T) : RedisList {
    val list = getList(key)
    list.add(*values)
    return list
}

/**
 * Creates a new [RedisList] with the given [key] and adds [values] to it.
 *
 * **Note**: If the list already exists, the values will be added to the end of the list
 * @return A [RedisList] for the given [key]
 */
suspend inline fun <reified T> RedisClient.putList(key: String, builder: MutableList<T>.() -> Unit) = putList(key, buildList(builder))