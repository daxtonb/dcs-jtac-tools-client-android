package com.daxtonb.dcsjtactoolsclient.model

class UnitLocation {
    var lat: Double = 0.0
    var long: Double = 0.0
    var alt: Double = 0.0
    var head: Double = 0.0

    override fun toString(): String {
        return "latitude: $lat\tlongitude: $long\t" +
                "altitude: $alt\theading: $head"
    }
}