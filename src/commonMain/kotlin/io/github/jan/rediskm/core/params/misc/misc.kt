package io.github.jan.rediskm.core.params.misc

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.measureTime
import com.soywiz.klock.milliseconds
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisElement
import io.github.jan.rediskm.core.entities.RedisListValue
import kotlinx.coroutines.sync.withLock

/**
 * Removes a timeout from a key
 *
 * @return True if the timeout was successfully removed, false if the key doesn't exist or the key doesn't have a timeout.
 */
suspend fun RedisElement.persist() = run {
    redisClient.sendCommand("PERSIST", key)
    redisClient.receive()!!.value == 1L
}

/**
 * Gets all keys given a [filter]
 */
suspend fun RedisElement.getKeys(filter: String = "*") = run {
    redisClient.sendCommand("KEYS", filter)
    (redisClient.receive() as RedisListValue).mapToStringList()
}

/**
 * Copies the value of this element to [newKey]
 *
 * @return True if the key was successfully copied, false if the key couldn't be copied.
 */
suspend fun RedisElement.copy(newKey: String) = run {
    redisClient.sendCommand("COPY", newKey)
    redisClient.receive()!!.value == 1L
}

/**
 * Deletes all [keys], if they exist
 *
 * @return The number of keys that were deleted.
 */
suspend fun RedisClient.delete(vararg keys: String) = run {
    sendCommand("DEL", *keys)
    receive()!!.value as Long
}

/**
 * Deletes this element
 */
suspend fun RedisElement.delete() = redisClient.delete(key)

/**
 * Checks how many keys of [keys] exist.
 *
 * @return The number of keys that exist.
 */
suspend fun RedisClient.exists(vararg keys: String) = run {
    sendCommand("EXISTS", *keys)
    receive()!!.value as Long
}

suspend fun RedisElement.exists() = redisClient.exists(key) == 1L

/**
 * Sets the [RedisElement.key] to expire after [timeout]
 */
suspend fun RedisElement.expire(timeout: TimeSpan): Boolean {
    redisClient.sendCommand("PEXPIRE", key, timeout.millisecondsLong)
    return redisClient.receive()!!.value == 1L
}

suspend fun RedisElement.expireAt(timestamp: DateTime): Boolean {
    redisClient.sendCommand("EXPIREAT", key, timestamp.unixMillisLong)
    return redisClient.receive()!!.value == 1L
}

/**
 * Gets the time until the key will be automatically removed.
 */
suspend fun RedisElement.getTTL(key: String): TimeSpan {
    redisClient.sendCommand("TTL", key)
    return (redisClient.receive()!!.value as Long).milliseconds
}

/**
 * Gets a random key
 */
suspend fun RedisClient.getRandomKey(): String? {
    sendCommand("RANDOMKEY")
    return receive()?.value?.toString()
}

/**
 * Renames the key [RedisElement.key] to [newKey]
 */
suspend fun RedisElement.rename(newKey: String): Boolean {
    redisClient.sendCommand("RENAME", key, newKey)
    return (redisClient.receive()!!.value == 1L).also {
        if(it) mutex.withLock { key = newKey }
    }
}

/**
 * Renames [RedisElement.key] to [newKey] only if [newKey] doesn't exist.
 */
suspend fun RedisElement.renameNX(newKey: String): Boolean {
    redisClient.sendCommand("RENAMENX", key, newKey)
    return (redisClient.receive()!!.value == 1L).also {
        if(it) mutex.withLock { key = newKey }
    }
}

suspend fun RedisElement.getType(): RedisElement.Type {
    redisClient.sendCommand("TYPE", key)
    return RedisElement.Type.valueOf(redisClient.receive()!!.value.toString().uppercase())
}