package io.github.jan.rediskm.core.entities.pipeline

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisListValue

sealed interface RedisTransaction {

    suspend fun queueCommands(vararg commands: RawCommand) = queueCommands(commands.toList())

    suspend fun queueCommands(commands: List<RawCommand>)

    suspend fun queueCommand(vararg args: String) = queueCommands(RawCommand(*args))

    suspend fun executeAll() : RedisListValue

    suspend fun discardAll()

}

internal class RedisTransactionImpl(private val redisClient: RedisClient, private val usePipeline: Boolean) : RedisTransaction {

    override suspend fun queueCommands(commands: List<RawCommand>) {
        if (usePipeline) {
            redisClient.pipeline.sendCommands(commands)
        } else {
            commands.forEach {
                redisClient.sendCommand(*it.args.toTypedArray())
                redisClient.receive()
            }
        }
    }

    override suspend fun executeAll(): RedisListValue {
        if(usePipeline) {
            redisClient.pipeline.receiveAll()
        }
        redisClient.sendCommand("EXEC")
        return redisClient.receive().fastCastTo<RedisListValue>()
    }

    override suspend fun discardAll() {
        redisClient.sendCommand("DISCARD")
        redisClient.receive()
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