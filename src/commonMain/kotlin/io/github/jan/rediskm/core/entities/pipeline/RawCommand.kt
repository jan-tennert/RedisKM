package io.github.jan.rediskm.core.entities.pipeline

data class RawCommand(val args: List<String>) {

    constructor(vararg args: String) : this(args.toList())

}