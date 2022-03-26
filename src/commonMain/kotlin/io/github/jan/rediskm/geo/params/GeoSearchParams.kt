package io.github.jan.rediskm.geo.params

import io.github.jan.rediskm.core.params.Params

class GeoSearchParams(override val args: MutableList<String>) : Params {

    /**
     * Uses the position of [member] as the center point
     */
    fun fromMember(member: String) {
        args.add("FROMMEMBER")
        args.add(member)
    }

    /*
     * Uses [lon] and [lat] as the center point
     */
    fun fromLonLat(lon: Double, lat: Double) {
        args.add("FROMLONLAT")
        args.add(lon.toString())
        args.add(lat.toString())
    }

    /**
     * Search inside a circular area according to the given [radius] in [unit]
     */
    fun byRadius(radius: Double, unit: GeoLocation.DistanceUnit) {
        args.add("BYRADIUS")
        args.add(radius.toString())
        args.add(unit.value)
    }

    /**
     * Search inside a rectangular area according to the given [width] and [height] in [unit]
     */
    fun byBox(width: Double, height: Double, unit: GeoLocation.DistanceUnit) {
        args.add("BYBOX")
        args.add(width.toString())
        args.add(height.toString())
        args.add(unit.value)
    }

    /**
     * For [GeoModule.searchAndStore] only.
     * Stores the distance in the destination sorted set instead of the geo-encoded hash
     */
    fun storeDist() {
        args.add("STOREDIST")
    }

    /**
     * Sort returned items from the nearest to the farthest, relative to the center point
     */
    fun ascending() {
        args.add("ASC")
    }

    /**
     * Sort returned items from the farthest to the nearest, relative to the center point
     */
    fun descending() {
        args.add("DESC")
    }

    /**
     * Only for [GeoModule.search]
     * Returns for each element the distance to the center point
     */
    fun withDistance() {
        args.add("WITHDIST")
    }

    /**
     * Only for [GeoModule.search]
     * Returns for each element the latitude and longitude of the element
     */
    fun withCoordinates() {
        args.add("WITHCOORD")
    }

    /**
     * Only for [GeoModule.search]
     * Returns for each element the geohash-encoded sorted set score of the element
     */
    fun withHash() {
        args.add("WITHHASH")
    }

}