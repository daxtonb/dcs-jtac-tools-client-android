package com.daxtonb.dcsjtactoolsclient

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "event")
data class CursorOnTarget @JvmOverloads constructor(
    @field:Attribute(name = "version", required = false)
    var version: String = "",

    @field:Attribute(name = "uid")
    var uid: String = "",

    @field:Attribute(name = "type")
    var type: String = "",

    @field:Attribute(name = "how")
    var how: String = "",

    @field:Attribute(name = "time")
    var time: String = "",

    @field:Attribute(name = "start")
    var start: String = "",

    @field:Attribute(name = "stale")
    var stale: String = "",

    @field:Element(name = "point")
    var point: Point = Point(),

    @field:Element(name = "detail")
    var detail: Detail = Detail()
)

@Root(name = "point", strict = false)
data class Point @JvmOverloads constructor(
    @field:Attribute(name = "lat")
    var lat: Double = 0.0,

    @field:Attribute(name = "lon")
    var lon: Double = 0.0,

    @field:Attribute(name = "ce", required = false)
    var ce: Double? = null,

    @field:Attribute(name = "hae")
    var hae: Double = 0.0,

    @field:Attribute(name = "le", required = false)
    var le: Double? = null
)

@Root(name = "detail", strict = false)
data class Detail @JvmOverloads constructor(
    @field:Element(name = "contact")
    var contact: Contact = Contact()
)

@Root(name = "contact", strict = false)
data class Contact @JvmOverloads constructor(
    @field:Attribute(name = "callsign")
    var callsign: String = ""
)