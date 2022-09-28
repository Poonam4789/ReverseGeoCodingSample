package com.example.reversegeocodingpoc.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PlacesApiModel(
    @SerializedName("features")
    @Expose
    var features: List<Feature>,
    @SerializedName("type")
    @Expose
    var type: String
) : Serializable

data class Feature(
    @SerializedName("geometry")
    @Expose
    var geometry: PlaceGeometry,
    @SerializedName("type")
    @Expose
    var type: String,
    @SerializedName("properties")
    @Expose
    var properties: Properties
)

data class PlaceGeometry(
    @SerializedName("coordinates")
    @Expose
    var coordinates: List<Double>,
    @SerializedName("type")
    @Expose
    var type: String
)

data class Properties(
    @SerializedName("osm_id")
    @Expose
    var osmId: Long,
    @SerializedName("osm_type")
    @Expose
    var osmType: String?,
    @SerializedName("country")
    @Expose
    var country: String?,
    @SerializedName("osm_key")
    @Expose
    var osmKey: String?,
    @SerializedName("osm_value")
    @Expose
    var osmValue: String?,
    @SerializedName("name")
    @Expose
    var name: String?,
    @SerializedName("type")
    @Expose
    var type: String?,
    @SerializedName("extent")
    @Expose
    var extent: List<Double>?,
    @SerializedName("city")
    @Expose
    var city: String?,
    @SerializedName("postcode")
    @Expose
    var postcode: String?,
    @SerializedName("subdistrict")
    @Expose
    var subdistrict: String?,
    @SerializedName("district")
    @Expose
    var district: String?,
    @SerializedName("suburb")
    @Expose
    var suburb: String?
)