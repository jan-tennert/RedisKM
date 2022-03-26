package io.github.jan.rediskm.geo.params

import io.github.jan.rediskm.core.params.Params

class GeoParams(override val args: MutableList<String>) : Params {

    fun xx() {
        args.add("xx")
    }

    fun nx() {
        args.add("nx")
    }

    fun ch() {
        args.add("ch")
    }

}