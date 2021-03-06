package io.github.jan.rediskm.core

import com.soywiz.korio.net.AsyncClient
import com.soywiz.korio.stream.writeString

suspend fun AsyncClient.writeCommand(args: List<Any>) {
    writeString(buildString {
        append("*")
        append(args.size.toString())
        append("\r\n")

        args.forEach {
            when(it) {
                is Number -> append(":$it\r\n")
                else -> append("$${it.toString().length}\r\n$it\r\n")
            }
        }
    })
}