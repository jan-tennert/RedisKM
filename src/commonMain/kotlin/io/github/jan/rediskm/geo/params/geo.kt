package io.github.jan.rediskm.geo.params

import com.soywiz.kds.fastCastTo
import io.github.jan.rediskm.core.RedisClient
import io.github.jan.rediskm.core.RedisModule
import io.github.jan.rediskm.core.entities.RedisDoubleValue
import io.github.jan.rediskm.core.entities.RedisIntegerValue
import io.github.jan.rediskm.core.entities.RedisListValue

class GeoModule internal constructor(val client: RedisClient) : RedisModule

val RedisClient.geo: GeoModule
    get() {
        return if(modules.containsKey("json")) {
            modules["json"] as GeoModule
        } else GeoModule(this).also {
            modules["json"] = it
        }
    }

data class GeoLocation(val longitude: Double, val latitude: Double, val name: String) {

    enum class DistanceUnit(val value: String) {
        METER("m"),
        KILOMETER("km"),
        MILES("mi"),
        FEET("ft")
    }

}

data class GeoQuery(val name: String, val longitude: Double? = null, val latitude: Double? = null, val distance: Double? = null, val hash: Long? = null)

/**
 * Adds the geospatial items to a sorted set
 * @param key the key of the sorted set
 */
suspend fun GeoModule.add(key: String, longitude: Double, latitude: Double, member: String, extraParams: GeoParams.() -> Unit = {}) =
    add(key, GeoLocation(longitude, latitude, member), extraParams = extraParams)

/**
 * Adds the geospatial items to a sorted set
 * @param key the key of the sorted set
 */
suspend fun GeoModule.add(key: String, vararg locations: GeoLocation, extraParams: GeoParams.() -> Unit = {}): Long {
    val params = buildList<String> {
        locations.forEach {
            if(it.longitude < -180 || it.longitude > 180) throw IllegalArgumentException("longitude must be between -180 and 180. Given: ${it.longitude}")
            if(it.latitude < -85 || it.latitude > 85) throw IllegalArgumentException("latitude must be between -85 and 85. Given: ${it.latitude}")
            add(it.longitude.toString())
            add(it.latitude.toString())
            add(it.name)
        }
        GeoParams(this).apply(extraParams)
    }
    client.sendCommand("GEOADD", key, *params.toTypedArray())
    return client.receive()?.value as Long
}

/**
 * Calculates the distances between to geospatial items
 * @param key the key of the sorted set
 * @param location1 the name of the first location
 * @param location2 the name of the second location
 * @param unit the unit of the distance
 */
suspend fun GeoModule.distanceBetween(key: String, location1: String, location2: String, unit: GeoLocation.DistanceUnit = GeoLocation.DistanceUnit.METER): Double {
    client.sendCommand("GEODIST", key, location1, location2, unit.value)
    return client.receive()?.value as Double
}

/**
 * Returns the hashes of the geospatial items
 */
suspend fun GeoModule.getHashes(key: String, vararg member: String): List<String> {
    client.sendCommand("GEOHASH", key, *member)
    return client.receive().fastCastTo<RedisListValue>().mapToStringList()
}

/**
 * Returns the location of the geospatial items
 */
suspend fun GeoModule.getPositions(key: String, vararg member: String): List<GeoLocation> {
    client.sendCommand("GEOPOS", key, *member)
    return client.receive().fastCastTo<RedisListValue>().mapIndexedTo(ArrayList()) { index, value ->
        value as RedisListValue
        val longitude = value.toList()[0]!!.value as Double
        val latitude = value.toList()[1]!!.value as Double
        GeoLocation(longitude, latitude, member[index])
    }
}

/**
 * Searches for geospatial items with the given [params]
 * @return a list of the found items
 */
suspend fun GeoModule.search(key: String, params: GeoSearchParams.() -> Unit): List<GeoQuery> {
    val extraParams = GeoSearchParams(mutableListOf()).apply(params)
    client.sendCommand("GEOSEARCH", key, *extraParams.args.toTypedArray())
    val queries = client.receive().fastCastTo<RedisListValue>()
    return if(queries.value.isNotEmpty() && queries.value[0] !is RedisListValue) {
        queries.map {
            GeoQuery(name = it!!.value.toString())
        }
    } else {
        queries.map {
            it as RedisListValue
            val name = it.toList()[0]!!.value as String
            var longitude: Double? = null
            var latitude: Double? = null
            var distance: Double? = null
            var hash: Long? = null

            it.drop(1).forEach { queryItemValue ->
                when(queryItemValue) {
                    is RedisDoubleValue -> {
                        distance = queryItemValue.value
                    }
                    is RedisIntegerValue -> {
                        hash = queryItemValue.value as Long
                    }
                    is RedisListValue -> {
                        longitude = queryItemValue.toList()[0]!!.value as Double
                        latitude = queryItemValue.toList()[1]!!.value as Double
                    }
                    else -> throw IllegalArgumentException("Unknown query item type: ${queryItemValue!!::class.simpleName}")
                }
            }
            GeoQuery(name, longitude, latitude, distance, hash)
        }
    }
}

/**
 * Searches for geospatial items with the given [params] and stores them in [destination]
 * @return the number of items stored
 */
suspend fun GeoModule.searchAndStore(key: String, destination: String, params: GeoSearchParams.() -> Unit): Long {
    val extraParams = GeoSearchParams(mutableListOf()).apply(params)
    client.sendCommand("GEOSEARCHSTORE", destination, key, *extraParams.args.toTypedArray())
    return client.receive()?.value as Long
}