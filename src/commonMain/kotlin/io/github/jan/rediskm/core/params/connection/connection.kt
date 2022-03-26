package io.github.jan.rediskm.core.params.connection

import com.soywiz.klock.TimeSpan
import com.soywiz.klock.measureTime
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisModule

class ConnectionModule internal constructor(val client: RedisClient) : RedisModule

val RedisClient.connection: ConnectionModule
    get() {
        return if(modules.containsKey("json")) {
            modules["json"] as ConnectionModule
        } else ConnectionModule(this).also {
            modules["json"] = it
        }
    }

suspend fun ConnectionModule.echo(message: String): String {
    client.sendCommand("ECHO", message)
    return client.receive()!!.value.toString()
}

/**
 * Pings the redis server
 * @return The amount it took to get a response form the server
 */
suspend fun ConnectionModule.ping() : TimeSpan {
    client.sendCommand("PING")
    return measureTime {
        client.receive()
    }
}