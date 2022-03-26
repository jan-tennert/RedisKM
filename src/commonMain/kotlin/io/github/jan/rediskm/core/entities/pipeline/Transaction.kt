package io.github.jan.rediskm.core.entities.pipeline

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisListValue

class RedisTransaction internal constructor(private val client: RedisClient, private val usePipeline: Boolean) {

    suspend fun queueCommands(vararg commands: RawCommand) {
        if (usePipeline) {
            client.pipeline.sendCommands(*commands)
        } else {
            commands.forEach {
                client.sendCommand(*it.args.toTypedArray())
                client.receive()
            }
        }
    }

    suspend fun execAll(): RedisListValue {
        if(usePipeline) {
            client.pipeline.receiveAll()
        }
        client.sendCommand("EXEC")
        return client.receive().fastCastTo<RedisListValue>()
    }

    suspend fun discardAll() {
        client.sendCommand("DISCARD")
        client.receive()
    }

}


suspend fun RedisClient.watchKey(vararg keys: String) {
    sendCommand("WATCH", *keys)
    receive()
}

suspend fun RedisClient.unwatchAllKeys() {
    sendCommand("UNWATCH")
    receive()
}