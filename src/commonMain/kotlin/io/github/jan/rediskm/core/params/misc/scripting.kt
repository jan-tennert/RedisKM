package io.github.jan.rediskm.core.params.misc

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisModule

class ScriptingModule(val client: RedisClient): RedisModule

val RedisClient.scripting: ScriptingModule
    get() {
        return if(modules.containsKey("lua")) {
            modules["lua"] as ScriptingModule
        } else ScriptingModule(this).also {
            modules["lua"] = it
        }
    }

/**
 * Loads a Lua script into the redis cache
 * @return The SHA1 digest of the script which can be used on [executeLoadedScript]
 */
suspend fun ScriptingModule.loadScript(script: String): String {
    client.sendCommand("SCRIPT", "LOAD", script)
    return client.receive()?.value.toString()
}

/**
 * Executes a Lua script which has been loaded into the redis cache
 */
suspend fun ScriptingModule.executeLoadedScript(sha1: String, vararg args: String): String {
    client.sendCommand("EVALSHA", sha1, *args)
    return client.receive()?.value.toString()
}

/**
 * Executes a Lua script which has been loaded into the redis cache
 */
suspend fun ScriptingModule.executeLoadedScript(sha1: String, map: Map<String, String>) = executeLoadedScript(sha1, map.size.toString(), *map.flatMap { listOf(it.key, it.value) }.toTypedArray())

private suspend fun ScriptingModule.clearScriptCache(type: String) {
    client.sendCommand("SCRIPT", "FLUSH", type)
    client.receive()
}

suspend fun ScriptingModule.clearScriptCacheAsync() = clearScriptCache("ASYNC")
suspend fun ScriptingModule.clearScriptCache() = clearScriptCache("SYNC")

suspend fun ScriptingModule.killRunningScript() {
    client.sendCommand("SCRIPT", "KILL")
    client.receive()
}

/**
 * Evaluates a lua script and returns the result
 */
suspend fun ScriptingModule.eval(script: String, vararg args: String): String {
    client.sendCommand("EVAL", script, *args)
    return client.receive()?.value.toString()
}

/**
 * Evaluates a lua script and returns the result
 */
suspend fun ScriptingModule.eval(script: String, map: Map<String, String>) = eval(script, map.size.toString(), *map.flatMap { listOf(it.key, it.value) }.toTypedArray())