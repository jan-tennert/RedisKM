package io.github.jan.rediskm.core.params.collection

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisListValue

/**
 * Gets multiple values at once
 */
@Suppress("UNCHECKED_CAST")
suspend fun RedisClient.getMultiple(vararg keys: String): RedisListValue {
    sendCommand("MGET", *keys)
    return (receive() as RedisListValue)
}

/**
 * Gets multiple values at once and converts them to [T]
 */
suspend fun <T> RedisClient.getMultiple(vararg keys: String, serializer: (String) -> T) = getMultiple(*keys).map(serializer)

/**
 * Sets multiple values at once
 */
suspend fun RedisClient.setMultiple(vararg values: Pair<String, Any>) {
    sendCommand("MSET", *values.map { listOf(it.first, it.second) }.flatten().toTypedArray())
    receive()
}

/**
 * Sets multiple values at once
 *
 * @return Returns true if all keys were set, and false if no key was set because on already existed.
 */
suspend fun RedisClient.setMultipleNX(vararg values: Pair<String, Any>): Boolean {
    sendCommand("MSETNX", *values.map { listOf(it.first, it.second) }.flatten().toTypedArray())
    val response = receive()!!.value as Long
    return response == 1L
}