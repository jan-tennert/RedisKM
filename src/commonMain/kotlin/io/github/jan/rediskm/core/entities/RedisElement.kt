package io.github.jan.rediskm.core.entities

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.params.get.get
import io.github.jan.rediskm.core.params.put.SetParams
import io.github.jan.rediskm.core.params.put.put

interface RedisElement {

    val key: String
    val redisClient: RedisClient

    enum class ElementType {
        STRING,
        LIST,
        SET,
        ZSET,
        HASH,
        STREAM
    }

}

class RedisElementImpl internal constructor(override val key: String, override val redisClient: RedisClient): RedisElement {

    override fun hashCode() = key.hashCode()
    override fun equals(other: Any?) = other is RedisElement && other.key == key
    override fun toString() = "RedisElement(key=$key)"


}

suspend inline fun <reified T> RedisElement.get(): T? = redisClient.get(key)

suspend inline fun <reified T> RedisElement.set(value: T, extraParams: SetParams.() -> Unit = {}) = redisClient.put(key, value, extraParams)

suspend inline fun <reified T> RedisElement.set(value: T, expirationDate: DateTime, extraParams: SetParams.() -> Unit = {}) = redisClient.put(key, value, expirationDate, extraParams)

suspend inline fun <reified T> RedisElement.set(value: T, expirationDuration: TimeSpan, extraParams: SetParams.() -> Unit = {}) = redisClient.put(key, value, expirationDuration, extraParams)