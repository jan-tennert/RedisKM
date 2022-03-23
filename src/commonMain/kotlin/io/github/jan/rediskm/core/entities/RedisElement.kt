package io.github.jan.rediskm.core.entities

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.params.get.get
import io.github.jan.rediskm.core.params.put.SetParams
import io.github.jan.rediskm.core.params.put.put
import kotlinx.coroutines.sync.Mutex

class RedisElement(val redisClient: RedisClient, key: String) {

    var key: String = key
        internal set
    internal val mutex = Mutex()

    suspend inline fun <reified T> get(): T? = redisClient.get(key)

    suspend inline fun <reified T> set(value: T, extraParams: SetParams.() -> Unit = {}) = redisClient.put(key, value, extraParams)

    suspend inline fun <reified T> set(value: T, expirationDate: DateTime, extraParams: SetParams.() -> Unit = {}) = redisClient.put(key, value, expirationDate, extraParams)

    suspend inline fun <reified T> set(value: T, expirationDuration: TimeSpan, extraParams: SetParams.() -> Unit = {}) = redisClient.put(key, value, expirationDuration, extraParams)

    enum class Type {
        STRING,
        LIST,
        SET,
        ZSET,
        HASH,
        STREAM
    }

}