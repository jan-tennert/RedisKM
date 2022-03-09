package io.github.jan.rediskm.params.misc

import io.github.jan.rediskm.RedisClient
import io.github.jan.rediskm.readResponse

/**
 * Removes a timeout from a key
 *
 * @return True if the timeout was successfully removed, false if the key doesn't exist or the key doesn't have a timeout.
 */
suspend fun RedisClient.persist(key: String) = sendCommand("PERSIST", key).run {
    val result = rawClient.readResponse() as Int
    result == 1
}

/**
 * Gets all keys given a [filter]
 */
suspend fun RedisClient.getKeys(filter: String = "*") = sendCommand("KEYS", filter).run {
    (rawClient.readResponse() as List<Any>).map(Any::toString)
}

/**
 * Copies the value of [key] to [newKey]
 *
 * @return True if the key was successfully copied, false if the key couldn't be copied.
 */
suspend fun RedisClient.copy(key: String, newKey: String) = sendCommand("COPY", key, newKey).run {
    val result = rawClient.readResponse() as Int
    result == 1
}

/**
 * Deletes all [keys], if they exist
 *
 * @return The number of keys that were deleted.
 */
suspend fun RedisClient.delete(vararg keys: String) = sendCommand("DEL", *keys).run {
    rawClient.readResponse() as Int
}

/**
 * Checks how many keys of [keys] exist.
 *
 * @return The number of keys that exist.
 */
suspend fun RedisClient.exists(vararg keys: String) = sendCommand("EXISTS", *keys).run {
    rawClient.readResponse() as Int
}