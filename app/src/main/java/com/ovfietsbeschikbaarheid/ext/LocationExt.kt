package com.ovfietsbeschikbaarheid.ext

import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import dev.jordond.compass.Coordinates
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun LocationOverviewModel.distanceTo(coordinates: Coordinates) = getDistanceFromLatLon(latitude, longitude, coordinates.latitude, coordinates.longitude)

/**
 * Returns the distance between the 2 coordinates in meters
 */
private fun getDistanceFromLatLon(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusMeters = 6378137.0 // Radius of the earth in meters
    val deltaLatitude = deg2rad(lat2 - lat1)
    val deltaLongitude = deg2rad(lon2 - lon1)

    val haversineLatitude = sin(deltaLatitude / 2) * sin(deltaLatitude / 2)
    val haversineLongitude = sin(deltaLongitude / 2) * sin(deltaLongitude / 2)

    val haversineFormula = haversineLatitude + cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * haversineLongitude

    val centralAngle = 2 * atan2(sqrt(haversineFormula), sqrt(1 - haversineFormula))

    return earthRadiusMeters * centralAngle
}

private fun deg2rad(degrees: Double): Double {
    return degrees * (Math.PI / 180)
}