package io.github.jan.rediskm.params.array

import io.github.jan.rediskm.RedisClient
import io.github.jan.rediskm.readResponse

/**
 * Gets multiple values at once
 */
@Suppress("UNCHECKED_CAST")
suspend fun RedisClient.getMultiple(vararg keys: String): List<Any> {
    sendCommand("MGET", *keys)
    return rawClient.readResponse() as List<Any>
}

/**
 * Gets multiple values at once and converts them to [T]
 */
suspend fun <T> RedisClient.getMultiple(vararg keys: String, serializer: (Any) -> T) = getMultiple(*keys).map(serializer)

/**
 * Sets multiple values at once
 */
suspend fun RedisClient.setMultiple(vararg values: Pair<String, Any>) {
    sendCommand("MSET", *values.map { listOf(it.first, it.second) }.flatten().toTypedArray())
    rawClient.readResponse()
}

/**
 * Sets multiple values at once
 *
 * @return Returns true if all keys were set, and false if no key was set because on already existed.
 */
suspend fun RedisClient.setMultipleNX(vararg values: Pair<String, Any>): Boolean {
    sendCommand("MSETNX", *values.map { listOf(it.first, it.second) }.flatten().toTypedArray())
    val response = rawClient.readResponse() as Int
    return response == 1
}