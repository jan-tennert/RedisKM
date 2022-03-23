package io.github.jan.rediskm.core.params.misc

import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisModule

class LuaModule(val client: RedisClient): RedisModule

val RedisClient.lua: LuaModule
    get() {
        return if(modules.containsKey("lua")) {
            modules["lua"] as LuaModule
        } else LuaModule(this).also {
            modules["lua"] = it
        }
    }

suspend fun LuaModule.loadScript(script: String): String {
    client.sendCommand("SCRIPT", "LOAD", script)
    return client.receive()?.value.toString()
}

suspend fun LuaModule.executeLoadedScript(sha1: String, vararg args: String): String {
    client.sendCommand("EVALSHA", sha1, *args)
    return client.receive()?.value.toString()
}

suspend fun LuaModule.executeLoadedScript(sha1: String, map: Map<String, String>) = executeLoadedScript(sha1, map.size.toString(), *map.flatMap { listOf(it.key, it.value) }.toTypedArray())

private suspend fun LuaModule.clearScriptCache(type: String) {
    client.sendCommand("SCRIPT", "FLUSH", type)
    client.receive()
}

suspend fun LuaModule.clearScriptCacheAsync() = clearScriptCache("ASYNC")
suspend fun LuaModule.clearScriptCache() = clearScriptCache("SYNC")

suspend fun LuaModule.killRunningScript() {
    client.sendCommand("SCRIPT", "KILL")
    client.receive()
}

suspend fun LuaModule.eval(script: String, vararg args: String): String {
    client.sendCommand("EVAL", script, *args)
    return client.receive()?.value.toString()
}

suspend fun LuaModule.eval(script: String, map: Map<String, String>) = eval(script, map.size.toString(), *map.flatMap { listOf(it.key, it.value) }.toTypedArray())