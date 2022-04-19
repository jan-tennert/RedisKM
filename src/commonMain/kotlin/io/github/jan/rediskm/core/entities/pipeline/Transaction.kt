package io.github.jan.rediskm.core.entities.pipeline

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.entities.RedisListValue

sealed interface RedisTransaction {

    /**
     * Queues multiple [commands] without executing them
     */
    suspend fun queueCommands(vararg commands: RawCommand) = queueCommands(commands.toList())

    /**
     * Queues multiple [commands] without executing them
     */
    suspend fun queueCommands(commands: List<RawCommand>)

    /**
     * Queues a command without executing him
     */
    suspend fun queueCommand(vararg args: String) = queueCommands(RawCommand(*args))

    /**
     * Executes all queued commands
     */
    suspend fun executeAll() : RedisListValue

    /**
     * Discards all queued commands
     */
    suspend fun discardAll()

}

internal class RedisTransactionImpl(private val redisClient: RedisClient, private val usePipeline: Boolean) : RedisTransaction {

    override suspend fun queueCommands(commands: List<RawCommand>) {
        if (usePipeline) {
            redisClient.pipeline.sendCommands(commands)
        } else {
            commands.forEach {
                redisClient.sendCommand(it.args[0], it.args.drop(1).toTypedArray())
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