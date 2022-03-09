package io.github.jan.rediskm.params.get

import io.github.jan.rediskm.params.Params

class GetExParams(override val args: MutableList<Any>) : Params {

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

    fun persist() {
        args.add("PERSIST")
    }

}