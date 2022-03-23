package io.github.jan.rediskm.core

import com.soywiz.klock.milliseconds
import com.soywiz.klock.seconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.delay
import com.soywiz.korio.net.AsyncClient
import io.github.jan.rediskm.core.entities.RedisValue
import io.github.jan.rediskm.core.logging.LoggerConfig
import io.github.jan.rediskm.core.logging.log
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
class RedisClient(private val host: String, private val port: Int, val username: String? = null, val password: String, val secure: Boolean = false, val loggerConfig: LoggerConfig = LoggerConfig()) {

    lateinit var rawClient: AsyncClient
    val mutex = Mutex()
    val pipeline = Pipeline()
    val modules = mutableMapOf<String, RedisModule>()
    private val LOGGER = Logger("RedisClient")
    var locked = false
        internal set

    init {
        LOGGER.level = loggerConfig.level
        LOGGER.output = loggerConfig.output
    }

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
        LOGGER.log(true, Logger.Level.INFO) {
            "Connecting to $host:$port"
        }
        try {
            rawClient = AsyncClient.createAndConnect(host, port, secure)
        } catch(_: Exception) {
            throw RedisException("Failed to connect to $host:$port")
        }
        LOGGER.log(true, Logger.Level.INFO) {
            "Successfully connected to to $host:$port!"
        }
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
        LOGGER.log(true, Logger.Level.INFO) {
            "Authenticating..."
        }
        val args = if(username != null) arrayOf(username, password) else arrayOf(password)
        val params = listOf("AUTH", *args).toTypedArray()
        sendCommand(*params)
        receive()
        LOGGER.log(true, Logger.Level.INFO) {
            "Successfully authenticated!"
        }
    }
    
    suspend fun receive(): RedisValue<*>? {
        return if(locked) {
            while(locked) {
                delay(1.milliseconds)
            }
            receive()
        } else {
            mutex.withLock { locked = true }
            rawClient.readResponse().also {
                mutex.withLock { locked = false }
                LOGGER.debug {
                    "Received response: $it"
                }
            }
        }
    }

    suspend fun sendCommand(vararg args: Any) {
        LOGGER.debug {
            "Sending command (args): ${args.joinToString(" ")}"
        }
        rawClient.writeCommand(args.map(Any::toString))
    }

    inner class Pipeline internal constructor() {

        var queuedCommands: Int = 0
            private set

        suspend fun queueCommands(commands: List<PipelineCommand>) = queueCommands(*commands.toTypedArray())

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