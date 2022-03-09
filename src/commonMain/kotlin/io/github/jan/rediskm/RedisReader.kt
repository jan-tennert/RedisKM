package io.github.jan.rediskm

import com.soywiz.korio.net.AsyncClient
import com.soywiz.korio.stream.readString
import com.soywiz.korio.stream.readStringz

suspend fun AsyncClient.readResponse(): Any = when(val char = read().toChar().also { println(it) }) {
    Constants.CHAR_PLUS -> readSimpleString()
    Constants.CHAR_DOLLAR -> readRedisBulkString()
    Constants.CHAR_COLON -> readNumber()
    Constants.CHAR_MINUS -> throw RedisException(readSimpleString())
    Constants.CHAR_STAR -> readArray() as Any
    else -> throw IllegalStateException("Unexpected char: $char")
}.also { println("RESPONSE: $it") }

private suspend fun AsyncClient.readSimpleString() = buildString {
    var value = read().toChar()
    while(value != '\r') {
        append(value)
        value = read().toChar()
    }
    value = read().toChar()
    if(value != '\n') throw IllegalStateException("Expected \\n at the end of a simple string")
}

private suspend fun AsyncClient.readRedisBulkString(): String {
    val length = buildString {
        var value = read().toChar()
        while(value != '\r') {
            append(value)
            value = read().toChar()
        }
    }.toInt()
    if(read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n after the length of a bulk string")
    val string = readString(length)
    if(read().toChar() != '\r' || read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n at the end of a bulk string")
    return string
}

private suspend fun AsyncClient.readArray() : List<Any> {
    println("hello")
    val length = buildString {
        var value = read().toChar()
        while(value != '\r') {
            append(value)
            value = read().toChar()
        }
    }.toInt()
    if(read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n after the length of an array")
    return buildList {
        for(i in 1..length) {
            add(readResponse())
        }
    }
}


private suspend fun AsyncClient.readNumber() = buildString {
    var value = read().toChar()
    while(value != '\r') {
        append(value)
        value = read().toChar()
    }
    if(read().toChar() != '\n') throw IllegalStateException("Expected \\r and \\n after the length of a bulk string")
}.toLong()