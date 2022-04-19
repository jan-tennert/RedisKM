package io.github.jan.rediskm.core.params.get

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisElementImpl
import io.github.jan.rediskm.core.utils.deserialize

/**
 * Get the value of [key].
 *
 * @param delete If true, the key will be deleted after getting it.
 * @param extraParams Pass in any additional parameters you want to send to the server.
 */
suspend inline fun <reified T> RedisClient.get(key: String, delete: Boolean = false, extraParams: GetExParams.() -> Unit = {}): T? {
    val params = GetExParams(mutableListOf()).apply(extraParams)
    val args = if(delete) {
        listOf("GETDEL", key)
    } else if(params.args.isNotEmpty()) {
        listOf("GETEX", key, *params.args.toTypedArray())
    } else {
        listOf("GET", key)
    }
    sendCommand(args[0], args.drop(1).toTypedArray())
    val result = receive() ?: return null
    return deserialize(result)
}

suspend fun RedisClient.getElement(key: String) = RedisElementImpl(key, this)