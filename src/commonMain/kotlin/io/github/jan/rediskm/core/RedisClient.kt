package io.github.jan.rediskm.core

import com.soywiz.klock.seconds
import com.soywiz.korio.async.delay
import com.soywiz.korio.net.AsyncClient
import io.github.jan.rediskm.core.entities.RedisValue
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Creates a new Redis client.
 *
 * @param host The hostname of the Redis server.
 * @param port The port of the Redis server.
 * @param password The password used to authenticated with the Redis server.
 * @param username The username used to authenticated with the Redis server. (optional)
 * @param secure Whether to use SSL or not. (optional, only available for JVM, JS and Windows)
 */
class RedisClient(private val host: String, private val port: Int, val username: String? = null, val password: String, val secure: Boolean = false) {

    lateinit var rawClient: AsyncClient
    val mutex = Mutex()
    val pipeline = Pipeline()
    val modules = mutableMapOf<String, RedisModule>()
    var locked = false
        internal set

    /**
     * Whether the client is connected to the server
     */
    val isConnected: Boolean
        get() = ::rawClient.isInitialized && rawClient.connected

    /**
     * Connects to the redis server
     *
     * @param auth Whether it should authenticate with the server after connecting. You can do that later with [authenticate]
     */
    suspend fun connect(auth: Boolean = true) {
        rawClient = AsyncClient.createAndConnect(host, port, secure)
        if(auth) authenticate()
    }

    /**
     * Disconnects from the redis server
     */
    suspend fun disconnect() {
        sendCommand("QUIT")
        receive()
        rawClient.close()
    }
    /**
     * Authenticate to the server.
     *
     * @throws RedisException if authentication fails.
     */
    suspend fun authenticate() {
        val args = if(username != null) arrayOf(username, password) else arrayOf(password)
        val params = listOf("AUTH", *args).toTypedArray()
        sendCommand(*params)
        receive()
    }
    
    suspend fun receive(): RedisValue<*>? {
        return if(locked) {
            while(locked) {
                delay(1.seconds)
            }
            receive()
        } else {
            mutex.withLock { locked = true }
            rawClient.readResponse().also {
                mutex.withLock { locked = false }
            }
        }
    }

    suspend fun sendCommand(vararg args: Any) {
        rawClient.writeCommand(args.map(Any::toString))
    }

    inner class Pipeline internal constructor() {

        var queuedCommands: Int = 0
            private set

        suspend fun queueCommands(vararg commands: PipelineCommand) {
            mutex.withLock {
                queuedCommands += commands.size
                locked = true
            }
            commands.forEach {
                sendCommand(*it.args.toTypedArray())
            }
        }

        suspend fun receiveAll(): List<RedisValue<*>?> {
            val responses = mutableListOf<RedisValue<*>?>()
            for(i in 0 until queuedCommands) {
                responses.add(rawClient.readResponse())
            }
            mutex.withLock {
                queuedCommands = 0
                locked = false
            }
            return responses
        }

    }

}

data class PipelineCommand(val args: List<String>) {

    constructor(vararg args: String) : this(args.toList())

}