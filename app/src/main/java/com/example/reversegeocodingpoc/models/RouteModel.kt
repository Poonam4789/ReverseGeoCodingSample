package com.example.reversegeocodingpoc.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class Root(
    @SerializedName("results")
    var customA: List<CustomA> = ArrayList(),
    @SerializedName("status")
    var status: String? = null
) : Serializable


data class CustomA(
    @SerializedName("geometry")
    var geometry: Geometry? = null,

    @SerializedName("vicinity")
    var vicinity: String? = null,

    @SerializedName("name")
    var name: String? = null
) : Serializable

data class Geometry(
    @SerializedName("location")
    var locationA: LocationA? = null
) : Serializable

data class LocationA(
    @SerializedName("lat")
    var lat: String? = null,
    @SerializedName("lng")
    var lng: String? = null
) : Serializable