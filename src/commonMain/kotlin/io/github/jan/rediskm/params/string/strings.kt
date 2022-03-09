package io.github.jan.rediskm.params.string

import io.github.jan.rediskm.RedisClient
import io.github.jan.rediskm.readResponse

/**
 * Append a string to the value from the [key].
 */
suspend fun RedisClient.append(key: String, value: String) = sendCommand("APPEND", key, value).run {
    rawClient.readResponse() as Long
}

/**
 * Gets the string between [start] and [end] from a key
 */
suspend fun RedisClient.substring(key: String, start: Long, end: Long) = sendCommand("GETRANGE", key, start.toString(), end.toString()).run {
    rawClient.readResponse() as String
}

/**
 * Replaces the string at [key] between [index] and the length of [value]
 */
suspend fun RedisClient.replaceAt(key: String, index: Int, value: String) = sendCommand("SETRANGE", key, index.toString(), value).run {
    rawClient.readResponse() as Long
}

/**
 * Gets the length of the string at [key]
 */
suspend fun RedisClient.getLength(key: String) = sendCommand("STRLEN", key).run {
    rawClient.readResponse() as Long
}