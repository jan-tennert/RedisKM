package io.github.jan.rediskm.params.set

import com.soywiz.klock.DateTime
import com.soywiz.klock.TimeSpan
import io.github.jan.rediskm.RedisClient
import io.github.jan.rediskm.readResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerOrNull
import kotlin.reflect.typeOf

/**
 * Set [key] to [value]
 *
 * @param key the key to set
 * @param value the value which will be set to [key]
 * @param extraParams Pass in any additional parameters you want to send to the server.
 */
suspend inline fun <reified T> RedisClient.set(key: String, value: T, extraParams: SetParams.() -> Unit = {}): Any {
    val serializedValue = if(serializerOrNull(typeOf<T>()) != null) {
        Json.encodeToString(value)
    } else {
        value.toString()
    }
    sendCommand("SET", *SetParams(mutableListOf(key, serializedValue)).also(extraParams).args.toTypedArray())
    return rawClient.readResponse()
}

/**
 * Set [key] to [value]
 *
 * @param key the key to set
 * @param value the value which will be set to [key]
 * @param expirationDate the expiration time of the key
 * @param extraParams Pass in any additional parameters you want to send to the server.
 */
suspend inline fun <reified T> RedisClient.set(key: String, value: T, expirationDate: DateTime, extraParams: SetParams.() -> Unit) = set(key, value) {
    apply(extraParams)
    pxAT(expirationDate.unixMillisLong)
}

/**
 * Set [key] to [value]
 *
 * @param key the key to set
 * @param value the value which will be set to [key]
 * @param expirationDuration the expiration duration of the key
 * @param extraParams Pass in any additional parameters you want to send to the server.
 */
suspend inline fun <reified T> RedisClient.set(key: String, value: T, expirationDuration: TimeSpan, extraParams: SetParams.() -> Unit) = set(key, value) {
    apply(extraParams)
    px(expirationDuration.millisecondsLong)
}