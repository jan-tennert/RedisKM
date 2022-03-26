package io.github.jan.rediskm.core.params.put

import io.github.jan.rediskm.core.params.Params

class SetParams(override val args: MutableList<String>) : Params {

    fun ex(seconds: Int) {
        args.addAll(listOf("EX", seconds.toString()))
    }

    fun px(milliseconds: Long) {
        args.addAll(listOf("PX", milliseconds.toString()))
    }

    fun exAt(seconds: Int) {
        args.addAll(listOf("EXAT", seconds.toString()))
    }

    fun pxAT(milliseconds: Long) {
        args.addAll(listOf("PXAT", milliseconds.toString()))
    }

    fun nx() {
        args.add("NX")
    }

    fun xx() {
        args.add("XX")
    }

    fun keepTTL() {
        args.add("KEEPTTL")
    }

    fun returnOldValue() {
        args.add("GET")
    }

}