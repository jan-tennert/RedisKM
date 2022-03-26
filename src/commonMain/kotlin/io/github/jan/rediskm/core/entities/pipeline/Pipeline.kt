package io.github.jan.rediskm.core.entities.pipeline

import com.soywiz.korio.util.buildList
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisValue
import io.github.jan.rediskm.core.readResponse
import kotlinx.coroutines.sync.withLock

class RedisPipeline internal constructor(private val client: RedisClient) {

    var queuedCommands: Int = 0
        private set

    suspend fun sendCommands(commands: Iterable<RawCommand>) = sendCommands(*commands.toList().toTypedArray())

    suspend fun sendCommands(vararg commands: RawCommand) {
        client.mutex.withLock {
            queuedCommands += commands.size
            client.locked = true
        }
        commands.forEach {
            client.sendCommand(*it.args.toTypedArray())
        }
    }

    suspend fun sendAndReceive(vararg commands: RawCommand): List<RedisValue<*>?> {
        sendCommands(*commands)
        return receiveAll()
    }

    suspend fun sendAndReceive(commands: Iterable<RawCommand>): List<RedisValue<*>?> {
        sendCommands(commands)
        return receiveAll()
    }

    suspend fun receiveAll() = buildList<RedisValue<*>?> {
        for (i in 0 until queuedCommands) {
            add(client.rawClient.readResponse())
        }
        client.mutex.withLock {
            queuedCommands = 0
            client.locked = false
        }
    }

}