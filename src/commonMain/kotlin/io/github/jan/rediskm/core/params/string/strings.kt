package io.github.jan.rediskm.core.params.string

import io.github.jan.rediskm.core.entities.RedisElement

/**
 * Append a string to the value from the [key].
 */
suspend fun RedisElement.append(key: String, value: String) = run {
    redisClient.sendCommand("APPEND", key, value)
    redisClient.receive()!!.value as Long
}

/**
 * Gets the string between [start] and [end] from a key
 */
suspend fun RedisElement.substring(key: String, start: Long, end: Long) = run {
    redisClient.sendCommand("GETRANGE", key, start.toString(), end.toString())
    redisClient.receive()!!.value as String
}

/**
 * Replaces the string at [key] between [index] and the length of [value]
 */
suspend fun RedisElement.replaceAt(key: String, index: Int, value: String) = run {
    redisClient.sendCommand("SETRANGE", key, index.toString(), value)
    redisClient.receive()!!.value as Long
}

/**
 * Gets the length of the string at [key]
 */
suspend fun RedisElement.getLength(key: String) = run {
    redisClient.sendCommand("STRLEN", key)
    redisClient.receive()!!.value as Long
}