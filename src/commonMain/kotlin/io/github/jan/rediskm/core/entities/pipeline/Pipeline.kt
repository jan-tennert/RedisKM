package io.github.jan.rediskm.core.entities.pipeline

import com.soywiz.korio.util.buildList
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisClientImpl
import io.github.jan.rediskm.core.entities.RedisValue
import io.github.jan.rediskm.core.readResponse
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface RedisPipeline {

    /**
     * Sends multiple commands to the server and does not wait for the responses.
     */
    suspend fun sendCommands(commands: Collection<RawCommand>)

    /**
     * Sends multiple commands to the server and does not wait for the responses.
     */
    suspend fun sendCommands(vararg commands: RawCommand) = sendCommands(commands.toList())

    /**
     * Sends multiple commands to the server and gets all responses after all commands are sent.
     */
    suspend fun sendAndReceive(vararg commands: RawCommand) = sendAndReceive(commands.toList())

    /**
     * Sends multiple commands to the server and gets all responses after all commands are sent.
     */
    suspend fun sendAndReceive(commands: Collection<RawCommand>): List<RedisValue<*>?>

    /**
     * Receives the responses of the last commands sent to the server.
     */
    suspend fun receiveAll(): List<RedisValue<*>?>

}

internal class RedisPipelineImpl(private val client: RedisClient) : RedisPipeline {

    var queuedCommands: Int = 0
    private val mutex = Mutex()

    override suspend fun sendCommands(vararg commands: RawCommand) = sendCommands(commands.toList())

    override suspend fun sendCommands(commands: Collection<RawCommand>) {
        mutex.withLock {
            queuedCommands += commands.size
            (client as RedisClientImpl).locked = true
        }
        commands.forEach {
            client.sendCommand(it.args[0], it.args.drop(1).toTypedArray())
        }
    }

    override suspend fun sendAndReceive(commands: Collection<RawCommand>): List<RedisValue<*>?> {
        sendCommands(commands)
        return receiveAll()
    }

    override suspend fun sendAndReceive(vararg commands: RawCommand): List<RedisValue<*>?> {
        sendCommands(*commands)
        return receiveAll()
    }

    override suspend fun receiveAll() = buildList<RedisValue<*>?> {
        for (i in 0 until queuedCommands) {
            add((client as RedisClientImpl).rawClient.readResponse())
        }
        mutex.withLock {
            queuedCommands = 0
            (client as RedisClientImpl).locked = false
        }
    }

}