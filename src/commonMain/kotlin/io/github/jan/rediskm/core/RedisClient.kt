package io.github.jan.rediskm.core

import com.soywiz.klock.milliseconds
import com.soywiz.klogger.Logger
import com.soywiz.korio.async.delay
import com.soywiz.korio.net.AsyncClient
import io.github.jan.rediskm.core.entities.RedisListValue
import io.github.jan.rediskm.core.entities.pipeline.RedisPipeline
import io.github.jan.rediskm.core.entities.pipeline.RedisTransaction
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
    val pipeline = RedisPipeline(this)
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
                println(it)
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

    suspend fun createTransaction(usePipeline: Boolean = true): RedisTransaction {
        sendCommand("MULTI")
        receive()
        return RedisTransaction(this, usePipeline)
    }

    suspend fun createTransaction(usePipeline: Boolean = true, init: suspend RedisTransaction.() -> Unit): RedisListValue {
        val transaction = createTransaction(usePipeline)
        init(transaction)
        return transaction.execAll()
    }

}
